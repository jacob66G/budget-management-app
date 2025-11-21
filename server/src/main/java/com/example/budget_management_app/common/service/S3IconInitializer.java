package com.example.budget_management_app.common.service;

import com.example.budget_management_app.config.IconConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3IconInitializer {

    private final S3Client s3Client;
    private final IconConfig iconConfig;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private static final String CATEGORY_PREFIX = "categories/";
    private static final String ACCOUNT_PREFIX = "accounts/";

    private static final String FILE_EXTENSION_PNG = ".png";
    private static final String FILE_EXTENSION_JPG = ".jpg";

    private enum IconType {
        ACCOUNTS, CATEGORIES
    }

//    @PostConstruct
//    public void init() {
//        log.info("Start loading icons from s3 bucket");
//        loadKeysByPrefix(ACCOUNT_PREFIX, IconType.ACCOUNTS);
//        loadKeysByPrefix(CATEGORY_PREFIX, IconType.CATEGORIES);
//        log.info("Successfully loaded icons. CATEGORIES:  {},  ACCOUNTS:  {}",
//                iconConfig.getCategories().size(),
//                iconConfig.getAccounts().size()
//        );
//    }

    private void loadKeysByPrefix(String prefix, IconType type) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        ListObjectsV2Response response;
        do {
            response = s3Client.listObjectsV2(request);

            for (S3Object s3Object : response.contents()) {
                String key = s3Object.key();

                if (key.endsWith(FILE_EXTENSION_JPG) || key.endsWith(FILE_EXTENSION_PNG)) {
                    if (type.equals(IconType.ACCOUNTS)) {
                        iconConfig.addAccountKey(key);
                    }
                    if (type.equals(IconType.CATEGORIES)) {
                        iconConfig.addCategoryKey(key);
                    }
                }
            }

        } while (response.isTruncated());
    }
}
