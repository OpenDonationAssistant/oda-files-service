package io.github.opendonationassistant.files;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.Optional;

@Controller("/files")
public class FilesController extends BaseController {

  private static final String DONATION_LISTENER_FILE = "DonationListener.zip";
  private static final String DONATION_LISTENER_BUCKET = "DonationListener";
  private static final String PUBLIC_BUCKET = "public";
  // NOTE: still a hardcoded identity (review issue, out of current scope)
  private static final String PRIVILEGED_UPLOADER = "nekromant80lvl";
  private static final String NO_SUCH_KEY = "NoSuchKey";
  private static final String NO_SUCH_BUCKET = "NoSuchBucket";

  private final ODALogger log = new ODALogger(this);
  private final MinioClient minio;
  private final FileAccessRepository fileAccessRepository;

  @Inject
  public FilesController(
    MinioClient minio,
    FileAccessRepository fileAccessRepository
  ) {
    this.minio = minio;
    this.fileAccessRepository = fileAccessRepository;
  }

  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Get(value = "/{name}", produces = { MediaType.APPLICATION_OCTET_STREAM })
  public HttpResponse<?> get(
    @PathVariable String name,
    @Nullable Authentication auth
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return HttpResponse.unauthorized();
    }

    if (DONATION_LISTENER_FILE.equals(name)) {
      var access = fileAccessRepository.findByRecipientIdAndFilenameAndBucket(
        owner.get(),
        DONATION_LISTENER_FILE,
        DONATION_LISTENER_BUCKET
      );
      if (access.isEmpty() || !access.get().allowed()) {
        log.info(
          "DonationListener access denied",
          Map.of("owner", owner.get())
        );
        return HttpResponse.unauthorized();
      }
      return streamObject(DONATION_LISTENER_BUCKET, name, owner.get());
    }

    return streamObject(owner.get(), name, owner.get());
  }

  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Put(
    value = "/{name}",
    consumes = { MediaType.MULTIPART_FORM_DATA },
    produces = { MediaType.TEXT_PLAIN }
  )
  public HttpResponse<?> put(
    @PathVariable String name,
    @Nullable @QueryValue("public") Boolean isPublic,
    CompletedFileUpload file,
    Authentication auth
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return HttpResponse.unauthorized();
    }

    var bucket = isPublic != null && isPublic ? PUBLIC_BUCKET : owner.get();
    if (DONATION_LISTENER_FILE.equals(name)) {
      if (!PRIVILEGED_UPLOADER.equals(owner.get())) {
        log.info(
          "DonationListener upload denied",
          Map.of("owner", owner.get())
        );
        return HttpResponse.status(HttpStatus.FORBIDDEN);
      }
      bucket = DONATION_LISTENER_BUCKET;
    }

    var mimeType = name.endsWith("css")
      ? "text/css"
      : MediaType.APPLICATION_OCTET_STREAM;
    log.info(
      "Uploading file",
      Map.of(
        "name",
        name,
        "owner",
        owner.get(),
        "isPublic",
        Optional.ofNullable(isPublic).orElse(false),
        "mime",
        mimeType
      )
    );

    try (var stream = file.getInputStream()) {
      minio.putObject(
        PutObjectArgs.builder()
          .bucket(bucket)
          .object(name)
          .contentType(mimeType)
          .stream(stream, file.getSize(), -1)
          .build()
      );
      return HttpResponse.ok();
    } catch (Exception e) {
      log.error(
        "Failed to upload file",
        Map.of(
          "bucket",
          bucket,
          "name",
          name,
          "owner",
          owner.get(),
          "error",
          e.getMessage()
        )
      );
      return HttpResponse.status(HttpStatus.BAD_GATEWAY);
    }
  }

  private HttpResponse<?> streamObject(
    String bucket,
    String name,
    String owner
  ) {
    try {
      GetObjectResponse object = minio.getObject(
        GetObjectArgs.builder().bucket(bucket).object(name).build()
      );
      return HttpResponse.ok(
        new StreamedFile(object, MediaType.APPLICATION_OCTET_STREAM_TYPE)
      );
    } catch (ErrorResponseException e) {
      if (isMissing(e)) {
        return HttpResponse.unauthorized();
      }
      return backendFailure(bucket, name, owner, e);
    } catch (Exception e) {
      return backendFailure(bucket, name, owner, e);
    }
  }

  private HttpResponse<?> backendFailure(
    String bucket,
    String name,
    String owner,
    Exception error
  ) {
    log.error(
      "Failed to get object",
      Map.of(
        "bucket",
        bucket,
        "name",
        name,
        "owner",
        owner,
        "error",
        error.getMessage()
      )
    );
    return HttpResponse.status(HttpStatus.BAD_GATEWAY);
  }

  private boolean isMissing(ErrorResponseException e) {
    var response = e.errorResponse();
    var code = response != null ? response.code() : null;
    return NO_SUCH_KEY.equals(code) || NO_SUCH_BUCKET.equals(code);
  }
}
