package com.spotfinder.backend.v1.reviews.application.internal.outboundservices.acl;

import com.spotfinder.backend.v1.profile.interfaces.acl.ProfilesContextFacade;
import org.springframework.stereotype.Service;

@Service("externalProfileServiceReview")
public class ExternalProfileService {
    private final ProfilesContextFacade profilesContextFacade;

    public ExternalProfileService(ProfilesContextFacade profilesContextFacade) {
        this.profilesContextFacade = profilesContextFacade;
    }

    public String getDriverFullNameByUserId(Long userId) {
        return this.profilesContextFacade.getDriverFullNameByUserId(userId);
    }
}
