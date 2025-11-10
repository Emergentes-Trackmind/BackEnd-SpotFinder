package com.spotfinder.backend.v1.profile.application.internal.queryservices;


import com.spotfinder.backend.v1.profile.domain.model.aggregates.ParkingOwner;
import com.spotfinder.backend.v1.profile.domain.model.queries.GetParkingOwnerByUserIdAsyncQuery;
import com.spotfinder.backend.v1.profile.domain.services.ParkingOwnerQueryService;
import com.spotfinder.backend.v1.profile.infrastructure.persistence.jpa.repositories.ProductOwnerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ParkingOwnerQueryServiceImpl implements ParkingOwnerQueryService {
    private final ProductOwnerRepository productOwnerRepository;

    public ParkingOwnerQueryServiceImpl(ProductOwnerRepository productOwnerRepository) {
        this.productOwnerRepository = productOwnerRepository;
    }

    @Override
    public Optional<ParkingOwner> handle(GetParkingOwnerByUserIdAsyncQuery query) {
        return productOwnerRepository.findParkingOwnerByUserId(query.userId());
    }
}
