package com.gamecity.scrabble.controller;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gamecity.scrabble.api.model.User;

/**
 * Image resources
 * 
 * @author ekarakus
 */
@RestController
@RequestMapping(value = "/image")
public class ImageController extends AbstractController {

    @Value("${avatar.path}")
    private String avatarPath;

    /**
     * Gets the thumbnail avatar of a {@link User}
     * 
     * @param id
     * @return the thumbnail image
     * @throws IOException if an exception is thrown during image processing
     */
    @GetMapping("/avatar/thumbnail/{id}")
    public ResponseEntity<InputStreamResource> getThumbnailAvatar(@PathVariable Long id) throws IOException {
        final InputStream inputStream = new BufferedInputStream(new FileInputStream(String.format(avatarPath, id, id)));
        InputStreamResource resource = new InputStreamResource(getThumbnailStream(inputStream, 75, 75));
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Gets the avatar of a {@link User}
     * 
     * @param id
     * @return the avatar image
     * @throws IOException if an exception is thrown during image processing
     */
    @GetMapping("/avatar/{id}")
    public ResponseEntity<InputStreamResource> getAvatar(@PathVariable Long id) throws IOException {
        final InputStream inputStream = new BufferedInputStream(new FileInputStream(String.format(avatarPath, id, id)));
        InputStreamResource resource = new InputStreamResource(getThumbnailStream(inputStream, 150, 150));
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    private InputStream getThumbnailStream(InputStream inputStream, int width, int height) throws IOException {
        final Image resizedImage = ImageIO.read(inputStream).getScaledInstance(width, height, Image.SCALE_DEFAULT);
        final BufferedImage bufferedResizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        bufferedResizedImage.getGraphics().drawImage(resizedImage, 0, 0, null);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedResizedImage, "png", baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

}
