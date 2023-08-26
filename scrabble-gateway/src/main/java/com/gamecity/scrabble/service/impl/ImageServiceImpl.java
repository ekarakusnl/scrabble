package com.gamecity.scrabble.service.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.gamecity.scrabble.rest.exception.GenericException;
import com.gamecity.scrabble.rest.exception.error.GenericError;
import com.gamecity.scrabble.service.ImageService;
import com.google.common.net.HttpHeaders;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
class ImageServiceImpl implements ImageService {

    private static final String TEMPORARY_FILE_NAME = "TEMP_";
    private static final String COMPRESSED_FILE_NAME = "COMPRESSED_";
    private static final String LINE_SEPERATOR = "\r\n";
    private static final Duration CDN_REQUEST_TIMEOUT = Duration.ofSeconds(15);

    @Value("${cdn.upload.image.endpoint}")
    private String cdnUploadImageEndpoint;

    @Value("${cdn.secret}")
    private String cdnSecret;

    @Override
    public void saveProfilePicture(Long userId, MultipartFile profilePicture) {
        final String fileName = String.valueOf(userId);
        final File temporaryFile = new File(TEMPORARY_FILE_NAME + profilePicture.getOriginalFilename());
        final File compressedFile = new File(COMPRESSED_FILE_NAME + profilePicture.getOriginalFilename());
        try {
            // store the image file in the disk temporarily
            profilePicture.transferTo(temporaryFile);
            compressProfilePicture(temporaryFile, compressedFile);

            final String boundary = "--------------------------" + RandomStringUtils.randomNumeric(24);

            final List<byte[]> formBody = new ArrayList<>();

            // top line
            formBody.add(("--" + boundary + LINE_SEPERATOR).getBytes(StandardCharsets.UTF_8));

            // set the fileName key
            formBody.add(("Content-Disposition: form-data; name=").getBytes(StandardCharsets.UTF_8));
            // set the fileName value
            formBody.add(("\"fileName\"" + LINE_SEPERATOR + LINE_SEPERATOR + fileName + LINE_SEPERATOR)
                    .getBytes(StandardCharsets.UTF_8));
            // spacer line
            formBody.add(("--" + boundary + LINE_SEPERATOR).getBytes(StandardCharsets.UTF_8));

            // set the useUniqueFileName key
            formBody.add(("Content-Disposition: form-data; name=").getBytes(StandardCharsets.UTF_8));
            // set the useUniqueFileName value
            formBody.add(("\"useUniqueFileName\"" + LINE_SEPERATOR + LINE_SEPERATOR + "false" + LINE_SEPERATOR)
                    .getBytes(StandardCharsets.UTF_8));
            // spacer line
            formBody.add(("--" + boundary + LINE_SEPERATOR).getBytes(StandardCharsets.UTF_8));

            // set the folder key
            formBody.add(("Content-Disposition: form-data; name=").getBytes(StandardCharsets.UTF_8));
            // set the folder value
            formBody.add(("\"folder\"" + LINE_SEPERATOR + LINE_SEPERATOR + "/scrabble/users" + LINE_SEPERATOR)
                    .getBytes(StandardCharsets.UTF_8));
            // spacer line
            formBody.add(("--" + boundary + LINE_SEPERATOR).getBytes(StandardCharsets.UTF_8));

            // set the file key
            formBody.add(("Content-Disposition: form-data; name=").getBytes(StandardCharsets.UTF_8));
            // set the file value
            formBody.add(("\"file\"; filename=\"" + fileName + "\"" + LINE_SEPERATOR).getBytes(StandardCharsets.UTF_8));
            // set the content type
            formBody.add(("Content-Type: image/png" + LINE_SEPERATOR + LINE_SEPERATOR).getBytes(StandardCharsets.UTF_8));
            // add the file
            formBody.add(Files.readAllBytes(compressedFile.toPath()));
            // add new line
            formBody.add((LINE_SEPERATOR).getBytes(StandardCharsets.UTF_8));

            // bottom line
            formBody.add(("--" + boundary + "--" + LINE_SEPERATOR).getBytes(StandardCharsets.UTF_8));

            final HttpRequest uploadPictureRequest = HttpRequest.newBuilder()
                    .uri(URI.create(cdnUploadImageEndpoint))
                    .POST(BodyPublishers.ofByteArrays(formBody))
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + cdnSecret)
                    .header(HttpHeaders.CONTENT_TYPE, "multipart/form-data; boundary=" + boundary)
                    .timeout(CDN_REQUEST_TIMEOUT)
                    .build();

            final HttpClient cdnClient = HttpClient.newHttpClient();
            final HttpResponse<?> response = cdnClient.send(uploadPictureRequest, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException(response.statusCode() + " - " + response.body());
            }

            log.debug(response.body().toString());
        } catch (Exception e) {
            log.error("An error occured while saving the image {}: {}", fileName, e.getMessage(), e);
            throw new GenericException(GenericError.PROFILE_PICTURE_UPDATE_FAILED.getCode(),
                    GenericError.PROFILE_PICTURE_UPDATE_FAILED.getMessage(), Arrays.asList(e.getMessage()));
        } finally {
            temporaryFile.delete();
            compressedFile.delete();
        }
    }

    private void compressProfilePicture(final File imageFile, final File compressedFile) throws IOException {
        final BufferedImage inputImage = ImageIO.read(imageFile);
        final Iterator<ImageWriter> writers =
                ImageIO.getImageWritersByMIMEType(Files.probeContentType(imageFile.toPath()));
        final ImageWriter writer = writers.next();

        final ImageOutputStream outputStream = ImageIO.createImageOutputStream(compressedFile);
        writer.setOutput(outputStream);

        final ImageWriteParam params = writer.getDefaultWriteParam();
        params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        params.setCompressionQuality(0.5f);

        writer.write(null, new IIOImage(inputImage, null, null), params);

        outputStream.close();
        writer.dispose();
    }

}
