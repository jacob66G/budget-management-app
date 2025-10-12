package com.example.budget_management_app.security.service;

import com.example.budget_management_app.common.service.CacheService;
import com.example.budget_management_app.common.service.RedisServiceImpl;
import com.example.budget_management_app.security.dto.UserCacheDto;
import com.example.budget_management_app.user.dao.UserDao;
import com.example.budget_management_app.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDao userDao;
    private final CacheService cacheService;
    @Value("${security.user-details-expiration}")
    private long userDetailsExpiration;

    @Override
    public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserCacheDto cached = (UserCacheDto) cacheService.getValue(RedisServiceImpl.KeyPrefix.USER_DETAILS, email);

        if (cached != null) {
            return new CustomUserDetails(
                    cached.getId(),
                    cached.getEmail(),
                    cached.getPassword()
            );
        }

        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email: " + email + " not found"));

        UserCacheDto dto = new UserCacheDto(user.getId(), user.getEmail(), user.getPassword());

        cacheService.storeValue(RedisServiceImpl.KeyPrefix.USER_DETAILS, email, dto, userDetailsExpiration);

        return new CustomUserDetails(user.getId(), user.getEmail(),  user.getPassword());
    }
}
