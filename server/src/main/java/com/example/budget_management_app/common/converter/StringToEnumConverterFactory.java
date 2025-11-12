package com.example.budget_management_app.common.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.lang.NonNull;

public class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {
    /**
     * Get the converter to convert from S to target type T, where T is also an instance of R.
     *
     * @param targetType the target type to convert to
     * @return a converter from S to T
     */
    @Override
    @NonNull
    public <T extends Enum> Converter<String, T> getConverter(@NonNull Class<T> targetType) {
        return new StringToEnumConverter<>(targetType);
    }

    private static class StringToEnumConverter<T extends Enum> implements Converter<String, T> {

        private final Class<T> enumType;

        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        /**
         * Convert the source object of type {@code S} to target type {@code T}.
         *
         * @param source the source object to convert, which must be an instance of {@code S} (never {@code null})
         * @return the converted object, which must be an instance of {@code T} (potentially {@code null})
         * @throws IllegalArgumentException if the source cannot be converted to the desired target type
         */
        @Override
        public T convert(@NonNull String source) {

            if (source.isEmpty()) {
                throw new IllegalArgumentException("Parameter value is required");
            }

            return (T) Enum.valueOf(this.enumType, source.trim().toUpperCase());
        }
    }
}
