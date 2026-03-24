package com.phumlanidev.cartservice.service.impl;

import com.phumlanidev.cartservice.dto.AuditLogDto;
import com.phumlanidev.cartservice.model.AuditLog;
import com.phumlanidev.cartservice.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogServiceImpl Tests")
class AuditLogServiceImplTest {

  // ─────────────────────────────────────────────
  // Mocks & Subject
  // ─────────────────────────────────────────────

  @Mock
  private AuditLogRepository auditLogRepository;

  @InjectMocks
  private AuditLogServiceImpl auditLogService;

  // ─────────────────────────────────────────────
  // Shared fixtures
  // ─────────────────────────────────────────────

  private static final String ACTION     = "ADD_PRODUCT_CART";
  private static final String USER_ID    = "user-123";
  private static final String USERNAME   = "phumlani";
  private static final String IP_ADDRESS = "127.0.0.1";
  private static final String DETAILS    = "Product added to cart: 10, Quantity: 2";

  // ═══════════════════════════════════════════════════════════════
  // log()
  // ═══════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("log()")
  class Log {

    @Test
    @DisplayName("saves an AuditLog with all provided fields")
    void shouldSaveAuditLogWithAllFields() {
      auditLogService.log(ACTION, USER_ID, USERNAME, IP_ADDRESS, DETAILS);

      ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
      verify(auditLogRepository).save(captor.capture());

      AuditLog saved = captor.getValue();
      assertThat(saved.getAction()).isEqualTo(ACTION);
      assertThat(saved.getUserId()).isEqualTo(USER_ID);
      assertThat(saved.getUsername()).isEqualTo(USERNAME);
      assertThat(saved.getIpAddress()).isEqualTo(IP_ADDRESS);
      assertThat(saved.getDetails()).isEqualTo(DETAILS);
    }

    @Test
    @DisplayName("sets a non-null timestamp on save")
    void shouldSetTimestampOnSave() {
      auditLogService.log(ACTION, USER_ID, USERNAME, IP_ADDRESS, DETAILS);

      ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
      verify(auditLogRepository).save(captor.capture());

      // We cannot assert the exact Instant.now() value, but it must not be null
      assertThat(captor.getValue().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("sets a timestamp close to the current time")
    void shouldSetTimestampCloseToNow() {
      Instant before = Instant.now();

      auditLogService.log(ACTION, USER_ID, USERNAME, IP_ADDRESS, DETAILS);

      Instant after = Instant.now();

      ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
      verify(auditLogRepository).save(captor.capture());

      Instant timestamp = captor.getValue().getTimestamp();
      assertThat(timestamp).isAfterOrEqualTo(before);
      assertThat(timestamp).isBeforeOrEqualTo(after);
    }

    @Test
    @DisplayName("calls repository save exactly once per log() invocation")
    void shouldCallSaveExactlyOnce() {
      auditLogService.log(ACTION, USER_ID, USERNAME, IP_ADDRESS, DETAILS);

      verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("saves correctly when optional fields (details) are null")
    void shouldSaveWhenDetailsIsNull() {
      auditLogService.log(ACTION, USER_ID, USERNAME, IP_ADDRESS, null);

      ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
      verify(auditLogRepository).save(captor.capture());

      assertThat(captor.getValue().getDetails()).isNull();
    }

    @Test
    @DisplayName("saves correctly when userId is null")
    void shouldSaveWhenUserIdIsNull() {
      auditLogService.log(ACTION, null, USERNAME, IP_ADDRESS, DETAILS);

      ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
      verify(auditLogRepository).save(captor.capture());

      assertThat(captor.getValue().getUserId()).isNull();
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // getAuditLogs()
  // ═══════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("getAuditLogs()")
  class GetAuditLogs {

    private Pageable pageable;
    private AuditLog sampleLog;

    @BeforeEach
    void setUp() {
      pageable = PageRequest.of(0, 10, Sort.by("timestamp").descending());

      sampleLog = AuditLog.builder()
              .id(1L)
              .action(ACTION)
              .userId(USER_ID)
              .username(USERNAME)
              .ipAddress(IP_ADDRESS)
              .details(DETAILS)
              .timestamp(Instant.parse("2024-06-01T10:00:00Z"))
              .build();
    }

    @Test
    @DisplayName("returns a mapped Page<AuditLogDto> from repository results")
    void shouldReturnMappedPageOfDtos() {
      Page<AuditLog> repoPage = new PageImpl<>(List.of(sampleLog), pageable, 1);
      when(auditLogRepository.findAll(any(Specification.class), eq(pageable)))
              .thenReturn(repoPage);

      Page<AuditLogDto> result = auditLogService.getAuditLogs(USER_ID, ACTION, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("maps all AuditLog fields correctly to AuditLogDto")
    void shouldMapAllFieldsCorrectly() {
      Page<AuditLog> repoPage = new PageImpl<>(List.of(sampleLog), pageable, 1);
      when(auditLogRepository.findAll(any(Specification.class), eq(pageable)))
              .thenReturn(repoPage);

      Page<AuditLogDto> result = auditLogService.getAuditLogs(USER_ID, ACTION, pageable);

      AuditLogDto dto = result.getContent().getFirst();
      assertThat(dto.getId()).isEqualTo("1");               // Long → String conversion
      assertThat(dto.getUserId()).isEqualTo(USER_ID);
      assertThat(dto.getUsername()).isEqualTo(USERNAME);
      assertThat(dto.getAction()).isEqualTo(ACTION);
      assertThat(dto.getIp()).isEqualTo(IP_ADDRESS);
      assertThat(dto.getDetails()).isEqualTo(DETAILS);
      assertThat(dto.getTimestamp()).isEqualTo(Instant.parse("2024-06-01T10:00:00Z"));
    }

    @Test
    @DisplayName("returns empty page when repository returns no results")
    void shouldReturnEmptyPageWhenNoResults() {
      Page<AuditLog> emptyPage = new PageImpl<>(List.of(), pageable, 0);
      when(auditLogRepository.findAll(any(Specification.class), eq(pageable)))
              .thenReturn(emptyPage);

      Page<AuditLogDto> result = auditLogService.getAuditLogs(USER_ID, ACTION, pageable);

      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("maps multiple results correctly")
    void shouldMapMultipleResultsCorrectly() {
      AuditLog secondLog = AuditLog.builder()
              .id(2L)
              .action("REMOVE_CART_ITEM")
              .userId(USER_ID)
              .username(USERNAME)
              .ipAddress(IP_ADDRESS)
              .details("Cart item removed: 5")
              .timestamp(Instant.parse("2024-06-01T11:00:00Z"))
              .build();

      Page<AuditLog> repoPage = new PageImpl<>(List.of(sampleLog, secondLog), pageable, 2);
      when(auditLogRepository.findAll(any(Specification.class), eq(pageable)))
              .thenReturn(repoPage);

      Page<AuditLogDto> result = auditLogService.getAuditLogs(USER_ID, ACTION, pageable);

      assertThat(result.getContent()).hasSize(2);
      assertThat(result.getContent().get(0).getId()).isEqualTo("1");
      assertThat(result.getContent().get(1).getId()).isEqualTo("2");
    }

    @Test
    @DisplayName("passes the correct Pageable to repository")
    void shouldPassCorrectPageableToRepository() {
      Page<AuditLog> emptyPage = new PageImpl<>(List.of(), pageable, 0);
      when(auditLogRepository.findAll(any(Specification.class), eq(pageable)))
              .thenReturn(emptyPage);

      auditLogService.getAuditLogs(USER_ID, ACTION, pageable);

      verify(auditLogRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("returns correct pagination metadata")
    void shouldReturnCorrectPaginationMetadata() {
      Pageable smallPage = PageRequest.of(0, 2);
      List<AuditLog> logs = List.of(sampleLog);
      Page<AuditLog> repoPage = new PageImpl<>(logs, smallPage, 10); // 10 total, page size 2

      when(auditLogRepository.findAll(any(Specification.class), eq(smallPage)))
              .thenReturn(repoPage);

      Page<AuditLogDto> result = auditLogService.getAuditLogs(USER_ID, ACTION, smallPage);

      assertThat(result.getTotalElements()).isEqualTo(10);
      assertThat(result.getTotalPages()).isEqualTo(5);
      assertThat(result.getNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("converts AuditLog id from Long to String in the dto")
    void shouldConvertIdFromLongToString() {
      AuditLog logWithLargeId = AuditLog.builder()
              .id(99999L)
              .action(ACTION).userId(USER_ID).username(USERNAME)
              .ipAddress(IP_ADDRESS).details(DETAILS)
              .timestamp(Instant.now())
              .build();

      Page<AuditLog> repoPage = new PageImpl<>(List.of(logWithLargeId), pageable, 1);
      when(auditLogRepository.findAll(any(Specification.class), eq(pageable)))
              .thenReturn(repoPage);

      Page<AuditLogDto> result = auditLogService.getAuditLogs(USER_ID, ACTION, pageable);

      assertThat(result.getContent().getFirst().getId()).isEqualTo("99999");
    }
  }
}