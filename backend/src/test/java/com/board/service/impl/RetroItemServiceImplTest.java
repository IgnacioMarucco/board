package com.board.service.impl;

import com.board.dto.retroitem.RetroItemCreateRequest;
import com.board.dto.retroitem.RetroItemResponse;
import com.board.dto.retroitem.RetroItemUpdateRequest;
import com.board.entity.Ceremony;
import com.board.entity.Project;
import com.board.entity.RetroItem;
import com.board.entity.Sprint;
import com.board.entity.User;
import com.board.entity.enums.CeremonyType;
import com.board.entity.enums.RetroCategory;
import com.board.exception.BadRequestException;
import com.board.mapper.RetroItemMapper;
import com.board.repository.CeremonyRepository;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.RetroItemRepository;
import com.board.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RetroItemServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class RetroItemServiceImplTest {

    @Mock
    private RetroItemRepository retroItemRepository;
    @Mock
    private CeremonyRepository ceremonyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private RetroItemMapper retroItemMapper;

    @InjectMocks
    private RetroItemServiceImpl retroItemService;

    private User testUser;
    private Project testProject;
    private Sprint testSprint;
    private Ceremony testCeremony;
    private RetroItem testRetroItem;
    private RetroItemResponse testResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@example.com").build();
        testProject = Project.builder().id(1L).key("TEST").owner(testUser).build();
        testSprint = Sprint.builder().id(1L).name("Sprint 1").project(testProject).build();
        testCeremony = Ceremony.builder()
                .id(1L)
                .type(CeremonyType.RETROSPECTIVE)
                .sprint(testSprint)
                .build();
        testRetroItem = RetroItem.builder()
                .id(1L)
                .content("Test item")
                .ceremony(testCeremony)
                .author(testUser)
                .build();
        testResponse = RetroItemResponse.builder().id(1L).build();
    }

    @Nested
    @DisplayName("createRetroItem")
    class CreateRetroItem {

        @Test
        @DisplayName("should create retro item successfully")
        void shouldCreateRetroItemSuccessfully() {
            // Given
            RetroItemCreateRequest request = RetroItemCreateRequest.builder()
                    .content("Test item")
                    .category(RetroCategory.START)
                    .isAnonymous(false)
                    .build();

            when(ceremonyRepository.findById(1L)).thenReturn(Optional.of(testCeremony));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);
            when(retroItemRepository.save(any(RetroItem.class))).thenReturn(testRetroItem);
            when(retroItemMapper.toResponse(any())).thenReturn(testResponse);

            // When
            RetroItemResponse response = retroItemService.createRetroItem(1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(retroItemRepository).save(any(RetroItem.class));
        }

        @Test
        @DisplayName("should throw BadRequestException when ceremony is not retrospective")
        void shouldThrowWhenNotRetrospective() {
            // Given
            testCeremony.setType(CeremonyType.PLANNING);
            RetroItemCreateRequest request = RetroItemCreateRequest.builder()
                    .content("Test item")
                    .category(RetroCategory.START)
                    .build();

            when(ceremonyRepository.findById(1L)).thenReturn(Optional.of(testCeremony));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> retroItemService.createRetroItem(1L, request, 1L))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("getRetroItemsForCeremony")
    class GetRetroItemsForCeremony {

        @Test
        @DisplayName("should return retro items for ceremony")
        void shouldReturnRetroItemsForCeremony() {
            // Given
            when(ceremonyRepository.findById(1L)).thenReturn(Optional.of(testCeremony));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);
            when(retroItemRepository.findByCeremonyOrderByVotesDescCreatedAtAsc(testCeremony))
                    .thenReturn(List.of(testRetroItem));
            when(retroItemMapper.toResponseList(any())).thenReturn(List.of(testResponse));

            // When
            List<RetroItemResponse> response = retroItemService
                    .getRetroItemsForCeremony(1L, 1L);

            // Then
            assertThat(response).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateRetroItem")
    class UpdateRetroItem {

        @Test
        @DisplayName("should update retro item successfully")
        void shouldUpdateRetroItemSuccessfully() {
            // Given
            RetroItemUpdateRequest request = RetroItemUpdateRequest.builder()
                    .content("Updated content")
                    .build();

            when(retroItemRepository.findById(1L)).thenReturn(Optional.of(testRetroItem));
            when(retroItemRepository.save(testRetroItem)).thenReturn(testRetroItem);
            when(retroItemMapper.toResponse(testRetroItem)).thenReturn(testResponse);

            // When
            RetroItemResponse response = retroItemService.updateRetroItem(1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(retroItemRepository).save(testRetroItem);
        }
    }

    @Nested
    @DisplayName("voteRetroItem")
    class VoteRetroItem {

        @Test
        @DisplayName("should add vote successfully")
        void shouldAddVoteSuccessfully() {
            // Given
            when(retroItemRepository.findById(1L)).thenReturn(Optional.of(testRetroItem));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);
            when(retroItemRepository.save(testRetroItem)).thenReturn(testRetroItem);
            when(retroItemMapper.toResponse(testRetroItem)).thenReturn(testResponse);

            // When
            RetroItemResponse response = retroItemService.voteRetroItem(1L, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(retroItemRepository).save(testRetroItem);
        }
    }

    @Nested
    @DisplayName("deleteRetroItem")
    class DeleteRetroItem {

        @Test
        @DisplayName("should soft delete retro item successfully")
        void shouldSoftDeleteRetroItemSuccessfully() {
            // Given
            when(retroItemRepository.findById(1L)).thenReturn(Optional.of(testRetroItem));

            // When
            retroItemService.deleteRetroItem(1L, 1L);

            // Then
            verify(retroItemRepository).save(testRetroItem);
            assertThat(testRetroItem.getDeletedAt()).isNotNull();
        }
    }
}
