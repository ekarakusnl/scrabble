package com.gamecity.scrabble.service;

import org.springframework.web.multipart.MultipartFile;

import com.gamecity.scrabble.api.model.User;

/**
 * Image operations such as upload/download images to/from CDN
 * 
 * @author ekarakus
 */
public interface ImageService {

    /**
     * Save the profile picture of the {@link User user}
     * 
     * @param userId         <code>id</code> of the user
     * @param profilePicture new profile picture of the user
     */
    void saveProfilePicture(Long userId, MultipartFile profilePicture);

}
