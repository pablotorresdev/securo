package com.mb.conitrack.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mb.conitrack.repository.LoteRepository;

@ExtendWith(MockitoExtension.class)
class LoteServiceTest {

    @Mock
    LoteRepository loteRepository;

    @InjectMocks
    LoteService service;



}