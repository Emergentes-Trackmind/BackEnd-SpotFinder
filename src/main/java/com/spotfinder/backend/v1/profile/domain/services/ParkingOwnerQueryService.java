package com.spotfinder.backend.v1.profile.domain.services;





import com.spotfinder.backend.v1.profile.domain.model.aggregates.ParkingOwner;
import com.spotfinder.backend.v1.profile.domain.model.queries.GetParkingOwnerByUserIdAsyncQuery;

import java.util.Optional;

public interface ParkingOwnerQueryService {
    Optional<ParkingOwner> handle(GetParkingOwnerByUserIdAsyncQuery query);
}
