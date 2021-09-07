package com.progress.application.project.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;

@RestClientTest(IssueTrackingService.class)
class IssueTrackingServiceTest {

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @MockBean
    private IssueTrackingService cut;

    @MockBean
    HtmlRenderingService htmlRenderingService;

    @Test
    void shouldInjectBeans() {
        assertThat(cut).isNotNull();
        assertThat(mockRestServiceServer).isNotNull();
    }
}