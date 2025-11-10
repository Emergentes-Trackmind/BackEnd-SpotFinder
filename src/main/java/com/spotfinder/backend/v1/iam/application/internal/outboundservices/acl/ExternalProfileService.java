package com.spotfinder.backend.v1.iam.application.internal.outboundservices.acl;



import com.spotfinder.backend.v1.profile.interfaces.acl.ProfilesContextFacade;
import org.springframework.stereotype.Service;

@Service
public class ExternalProfileService {
    private final ProfilesContextFacade profileContextFacade;

    public ExternalProfileService(ProfilesContextFacade profileContextFacade) {
        this.profileContextFacade = profileContextFacade;
    }


    public Long createDriver(String fullName, String city, String country,
                             String phone, String dni, Long userId) {
        return profileContextFacade.createDriver(
                fullName, city, country, phone, dni, userId
        );
    }

    public Long createParkingOwner(String fullName, String city, String country,
                                   String phone, String companyName, String ruc, Long userId){
        return profileContextFacade.createParkingOwner(
                fullName, city, country, phone, companyName, ruc, userId
        );
    }
}
