package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.repository.AddressEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressEntityRepository addressEntityRepository;
}
