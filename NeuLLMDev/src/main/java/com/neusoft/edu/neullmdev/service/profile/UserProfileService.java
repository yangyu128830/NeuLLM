package com.neusoft.edu.neullmdev.service.profile;

import com.neusoft.edu.neullmdev.dto.profile.UserProfileLookupResult;
import com.neusoft.edu.neullmdev.entity.profile.UserProfileEntity;
import com.neusoft.edu.neullmdev.mapper.profile.UserProfileMapper;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    private final UserProfileMapper userProfileMapper;

    public UserProfileService(UserProfileMapper userProfileMapper) {
        this.userProfileMapper = userProfileMapper;
    }

    public UserProfileLookupResult lookupByName(String name) {
        if (name == null || name.isBlank()) {
            return UserProfileLookupResult.missingName();
        }
        UserProfileEntity row = userProfileMapper.findLatestByRealOrDisplayName(name.trim());
        if (row == null) {
            return UserProfileLookupResult.notFound();
        }
        String email = row.getEmail() != null && !row.getEmail().isBlank()
                ? row.getEmail().trim() : null;
        String phone = row.getPhone() != null && !row.getPhone().isBlank()
                ? row.getPhone().trim() : null;
        return UserProfileLookupResult.found(
                row.getId(),
                row.getRealName(),
                row.getDisplayName(),
                email,
                phone);
    }
}
