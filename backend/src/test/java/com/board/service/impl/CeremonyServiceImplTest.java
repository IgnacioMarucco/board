package com.board.service.impl;

import com.board.dto.ceremony.CeremonyCreateRequest;
import com.board.dto.ceremony.CeremonyResponse;
import com.board.dto.ceremony.CeremonyUpdateRequest;
import com.board.entity.Ceremony;
import com.board.entity.Project;
import com.board.entity.Sprint;
import com.board.entity.User;
import com.board.entity.enums.CeremonyStatus;
import com.board.entity.enums.CeremonyType;
import com.board.exception.NotFoundException;
import com.board.mapper.CeremonyMapper;
import com.board.repository.CeremonyRepository;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.SprintRepository;
import com.board.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CeremonyServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class CeremonyServiceImplTest {

    @Mock
    private CeremonyRepository ceremonyRepository;
    @Mock
    private SprintRepository sprintRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private CeremonyMapper ceremonyMapper;

    @InjectMocks
    private CeremonyServiceImpl ceremonyService;

    private User testUser;
    private Project testProject;
    private Sprint testSprint;
    private Ceremony testCeremony;
    private CeremonyResponse testResponse;
    private com.board.entity.ProjectMember scrumMasterMember;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@example.com").build();
        testProject = Project.builder().id(1L).key("TEST").owner(testUser).build();
        testSprint = Sprint.builder().id(1L).name("Sprint 1").project(testProject).build();
        testCeremony = Ceremony.builder()
                .id(1L)
                .type(CeremonyType.PLANNING)
                .sprint(testSprint)
                .build();
        testResponse = CeremonyResponse.builder().id(1L).build();
        scrumMasterMember = com.board.entity.ProjectMember.builder()
                .id(10L)
                .user(testUser)
                .project(testProject)
                .role(com.board.entity.enums.Role.SCRUM_MASTER)
                .build();
    }

    @Nested
    @DisplayName("createCeremony")
    class CreateCeremony {

        @Test
        @DisplayName("should create ceremony successfully")
        void shouldCreateCeremonySuccessfully() {
            // Given
            CeremonyCreateRequest request = CeremonyCreateRequest.builder()
                    .type(CeremonyType.PLANNING)
                    .scheduledAt(LocalDateTime.now().plusDays(1))
                    .build();

            when(sprintRepository.findById(1L)).thenReturn(Optional.of(testSprint));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(scrumMasterMember));
            when(ceremonyRepository.save(any(Ceremony.class))).thenReturn(testCeremony);
            when(ceremonyMapper.toResponse(any())).thenReturn(testResponse);

            // When
            CeremonyResponse response = ceremonyService.createCeremony(1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(ceremonyRepository).save(any(Ceremony.class));
        }
    }

    @Nested
    @DisplayName("getCeremony")
    class GetCeremony {

        @Test
        @DisplayName("should get ceremony successfully")
        void shouldGetCeremonySuccessfully() {
            // Given
            when(ceremonyRepository.findById(1L)).thenReturn(Optional.of(testCeremony));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);
            when(ceremonyMapper.toResponse(testCeremony)).thenReturn(testResponse);

            // When
            CeremonyResponse response = ceremonyService.getCeremony(1L, 1L);

            // Then
            assertThat(response).isNotNull();
        }
    }

    @Nested
    @DisplayName("getCeremoniesForSprint")
    class GetCeremoniesForSprint {

        @Test
        @DisplayName("should return ceremonies for sprint")
        void shouldReturnCeremoniesForSprint() {
            // Given
            when(sprintRepository.findById(1L)).thenReturn(Optional.of(testSprint));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);
            when(ceremonyRepository.findBySprintOrderByScheduledAtAsc(testSprint))
                    .thenReturn(List.of(testCeremony));
            when(ceremonyMapper.toResponseList(any())).thenReturn(List.of(testResponse));

            // When
            List<CeremonyResponse> response = ceremonyService.getCeremoniesForSprint(1L, 1L);

            // Then
            assertThat(response).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateCeremony")
    class UpdateCeremony {

        @Test
        @DisplayName("should update ceremony successfully")
        void shouldUpdateCeremonySuccessfully() {
            // Given
            CeremonyUpdateRequest request = CeremonyUpdateRequest.builder()
                    .status(CeremonyStatus.COMPLETED)
                    .build();

            when(ceremonyRepository.findById(1L)).thenReturn(Optional.of(testCeremony));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(scrumMasterMember));
            when(ceremonyRepository.save(testCeremony)).thenReturn(testCeremony);
            when(ceremonyMapper.toResponse(testCeremony)).thenReturn(testResponse);

            // When
            CeremonyResponse response = ceremonyService.updateCeremony(1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(ceremonyRepository).save(testCeremony);
        }
    }

    @Nested
    @DisplayName("deleteCeremony")
    class DeleteCeremony {

        @Test
        @DisplayName("should soft delete ceremony successfully")
        void shouldSoftDeleteCeremonySuccessfully() {
            // Given
            when(ceremonyRepository.findById(1L)).thenReturn(Optional.of(testCeremony));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(scrumMasterMember));

            // When
            ceremonyService.deleteCeremony(1L, 1L);

            // Then
            verify(ceremonyRepository).save(testCeremony);
            assertThat(testCeremony.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw NotFoundException when ceremony not found")
        void shouldThrowWhenCeremonyNotFound() {
            // Given
            when(ceremonyRepository.findById(1L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> ceremonyService.deleteCeremony(1L, 1L))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
