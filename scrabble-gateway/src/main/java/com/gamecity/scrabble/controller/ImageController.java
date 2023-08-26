package com.gamecity.scrabble.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gamecity.scrabble.service.ImageService;

/**
 * Image operations resources
 * 
 * @author ekarakus
 */
@RestController
@RequestMapping("/rest/images")
public class ImageController extends AbstractController {

    @Autowired
    private ImageService imageService;

    /**
     * 
     * @param profilePicture new profile picture of the user
     * @return successful operation result
     */
    @PostMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveProfilePicture(@RequestParam("file") MultipartFile profilePicture) {
        imageService.saveProfilePicture(getUserId(), profilePicture);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
