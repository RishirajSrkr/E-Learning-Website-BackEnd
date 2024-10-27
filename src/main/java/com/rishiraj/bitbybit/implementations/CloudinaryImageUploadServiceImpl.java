package com.rishiraj.bitbybit.implementations;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryImageUploadServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryImageUploadServiceImpl.class);
    @Autowired
    private Cloudinary cloudinary;


    public Map uploadImage(MultipartFile file) throws IOException {

        log.info("----------------------------------------");
        log.info("Multipart file {} ", file);
        log.info("----------------------------------------");


        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
    }

}
