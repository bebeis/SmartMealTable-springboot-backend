package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.repository.AddressEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

    private final AddressEntityRepository addressEntityRepository;
}
