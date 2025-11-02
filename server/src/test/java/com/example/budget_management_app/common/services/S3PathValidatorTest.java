package com.example.budget_management_app.common.services;

import com.example.budget_management_app.common.service.S3PathValidator;
import com.example.budget_management_app.common.service.S3PathValidatorImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = S3PathValidatorImpl.class)
@TestPropertySource(properties = {
        "aws.s3.bucket-name=test-bucket",
        "aws.region=eu-test-1"
})
class S3PathValidatorTest {

    @Autowired
    private S3PathValidator validator;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String REGION = "eu-test-1";
    private static final String EXPECTED_HOST = String.format("%s.s3.%s.amazonaws.com", BUCKET_NAME, REGION);

    private String buildUrl(String path) {
        return String.format("https://%s%s", EXPECTED_HOST, path);
    }

    private String buildUrlWithHost(String host, String path) {
        return String.format("https://%s%s", host, path);
    }

    @Test
    void should_return_true_for_valid_category_path_png() {
        // given
        String validUrl = buildUrl("/categories/food.png");

        // when
        boolean result = validator.isValidPathForCategory(validUrl);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void should_return_true_for_valid_account_path_jpg() {
        // given
        String validUrl = buildUrl("/accounts/my-wallet.jpg");

        // when
        boolean result = validator.isValidPathForAccount(validUrl);

        //then
        assertThat(result).isTrue();
    }

    @Test
    void should_return_true_for_uppercase_extension() {
        //given
        String validUrl = buildUrl("/categories/test.PNG");

        // when
        boolean result = validator.isValidPathForCategory(validUrl);

        //then
        assertThat(result).isTrue();
    }


    @ParameterizedTest
    @NullAndEmptySource
    void should_return_false_for_null_or_empty(String urlString) {
        assertThat(validator.isValidPathForCategory(urlString)).isFalse();
        assertThat(validator.isValidPathForAccount(urlString)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not-a-url",
            "htp://missing-protocol.com/image.png",
            "https://just.a.domain"
    })
    void should_return_false_for_malformed_url(String malformedUrl) {
        assertThat(validator.isValidPathForCategory(malformedUrl)).isFalse();
    }

    @Test
    void should_return_false_for_wrong_host() {
        // given
        String wrongHostUrl = buildUrlWithHost("wrong-bucket.s3.eu-test-1.amazonaws.com", "/categories/test.png");

        // when
        boolean result = validator.isValidPathForCategory(wrongHostUrl);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_for_non_aws_host() {
        // given
        String nonAwsUrl = "https://storage.googleapis.com/my-bucket/categories/test.png";

        // when
        boolean result = validator.isValidPathForCategory(nonAwsUrl);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_for_wrong_prefix() {
        // given
        String accountUrl = buildUrl("/accounts/test.png");
        String categoryUrl = buildUrl("/categories/test.png");

        // when
        boolean result = validator.isValidPathForCategory(accountUrl);
        boolean resultAcc = validator.isValidPathForAccount(categoryUrl);

        // then
        assertThat(result).isFalse();
        assertThat(resultAcc).isFalse();
    }

    @Test
    void should_return_false_for_path_with_subdirectory() {
        // given
        String subfolderUrl = buildUrl("/categories/subfolder/test.png");

        // when
        boolean result = validator.isValidPathForCategory(subfolderUrl);

        //then
        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_for_empty_filename() {
        // given
        String folderOnlyUrl = buildUrl("/categories/");

        // when
        boolean result = validator.isValidPathForCategory(folderOnlyUrl);

        //then
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/categories/image.gif",
            "/categories/image.txt",
            "/categories/image"
    })
    void should_return_false_for_invalid_extension(String invalidPath) {
        // given
        String url = buildUrl(invalidPath);

        // when
        boolean result = validator.isValidPathForCategory(url);

        // then
        assertThat(result).isFalse();
    }
}
