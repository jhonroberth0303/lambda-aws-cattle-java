package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.ProfileLifecycle;
import com.cattle.enums.LogType;
import com.cattle.enums.profiles.BovineCategory;
import com.cattle.enums.profiles.LifeStage;
import com.cattle.enums.profiles.LifecycleStatus;
import com.cattle.enums.profiles.Source;
import com.cattle.exceptions.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("repository")
class ProfileLifecycleRepositoryTest {

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<ProfileLifecycle> table;

    @Mock
    private PageIterable<ProfileLifecycle> pageIterable;

    @Mock
    private SdkIterable<ProfileLifecycle> sdkIterable;

    private ProfileLifecycleRepository repository;

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(enhancedClient.table(any(), any(TableSchema.class))).thenReturn(table);
        repository = new ProfileLifecycleRepository(lambdaContext, enhancedClient);
    }

    @Test
    void findById_existingItem_returnsOptional() {
        ArgumentCaptor<Key> captor = ArgumentCaptor.forClass(Key.class);
        ProfileLifecycle expected = createProfileLifecycle();
        when(table.getItem(captor.capture())).thenReturn(expected);

        Optional<ProfileLifecycle> result = repository.findById("BOVINE#101", "LIFECYCLE#ACTIVE");

        assertTrue(result.isPresent());
        assertSame(expected, result.get());
        assertEquals("BOVINE#101", captor.getValue().partitionKeyValue().s());
        assertEquals("LIFECYCLE#ACTIVE", captor.getValue().sortKeyValue().orElseThrow().s());
    }

    @Test
    void findById_resourceNotFound_returnsEmptyOptional() {
        when(table.getItem(any(Key.class)))
                .thenThrow(ResourceNotFoundException.builder().message("missing table").build());

        Optional<ProfileLifecycle> result = repository.findById("BOVINE#101", "LIFECYCLE#ACTIVE");

        assertTrue(result.isEmpty());
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("ProfileLifecycle table not found"), any(ResourceNotFoundException.class));
    }

    @Test
    void findById_unexpectedException_throwsRepositoryException() {
        when(table.getItem(any(Key.class))).thenThrow(new RuntimeException("boom"));

        assertThrows(RepositoryException.class,
                () -> repository.findById("BOVINE#101", "LIFECYCLE#ACTIVE"));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("findById error"), any(RuntimeException.class));
    }

    @Test
    void findAll_withResults_returnsList() {
        List<ProfileLifecycle> items = List.of(createProfileLifecycle(), createProfileLifecycle("BOVINE#102"));
        when(table.scan()).thenReturn(pageIterable);
        when(pageIterable.items()).thenReturn(sdkIterable);
        doAnswer(invocation -> {
            java.util.function.Consumer<ProfileLifecycle> consumer = invocation.getArgument(0);
            items.forEach(consumer);
            return null;
        }).when(sdkIterable).forEach(any());

        List<ProfileLifecycle> result = repository.findAll();

        assertEquals(2, result.size());
        assertEquals("BOVINE#101", result.get(0).getPk());
        assertEquals("BOVINE#102", result.get(1).getPk());
    }

    @Test
    void findAll_dynamoDbException_throwsRepositoryException() {
        when(table.scan()).thenThrow(DynamoDbException.builder().message("scan failed").build());

        assertThrows(RepositoryException.class, repository::findAll);

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error finding all ProfileLifecycle"), any(DynamoDbException.class));
    }

    @Test
    void save_validEntity_returnsSameEntity() {
        ProfileLifecycle entity = createProfileLifecycle();
        doNothing().when(table).putItem(entity);

        Optional<ProfileLifecycle> result = repository.save(entity);

        assertTrue(result.isPresent());
        assertSame(entity, result.get());
        verify(table).putItem(entity);
    }

    @Test
    void save_dynamoDbException_throwsRepositoryException() {
        ProfileLifecycle entity = createProfileLifecycle();
        doThrow(DynamoDbException.builder().message("save failed").build()).when(table).putItem(entity);

        assertThrows(RepositoryException.class, () -> repository.save(entity));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error saving ProfileLifecycle"), any(DynamoDbException.class));
    }

    @Test
    void update_validEntity_returnsSameEntity() {
        ProfileLifecycle entity = createProfileLifecycle();
        doNothing().when(table).putItem(entity);

        Optional<ProfileLifecycle> result = repository.update(entity);

        assertTrue(result.isPresent());
        assertSame(entity, result.get());
    }

    @Test
    void deleteById_buildsKeyCorrectly() {
        ArgumentCaptor<Key> captor = ArgumentCaptor.forClass(Key.class);

        repository.deleteById("BOVINE#101", "LIFECYCLE#ACTIVE");

        verify(table).deleteItem(captor.capture());
        assertEquals("BOVINE#101", captor.getValue().partitionKeyValue().s());
        assertEquals("LIFECYCLE#ACTIVE", captor.getValue().sortKeyValue().orElseThrow().s());
    }

    @Test
    void deleteById_exception_throwsRepositoryException() {
        doThrow(new RuntimeException("delete failed")).when(table).deleteItem(any(Key.class));

        assertThrows(RepositoryException.class,
                () -> repository.deleteById("BOVINE#101", "LIFECYCLE#ACTIVE"));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error deleting ProfileLifecycle by Id"), any(RuntimeException.class));
    }

    private ProfileLifecycle createProfileLifecycle() {
        return createProfileLifecycle("BOVINE#101");
    }

    private ProfileLifecycle createProfileLifecycle(String pk) {
        return ProfileLifecycle.builder()
                .pk(pk)
                .sk("LIFECYCLE#ACTIVE")
                .gsi1pk("LIFECYCLE")
                .gsi1sk(pk)
                .lifeStage(LifeStage.ADULT)
            .lifeStageSource(Source.AUTO)
                .category(BovineCategory.COW)
            .categorySource(Source.AUTO)
            .status(LifecycleStatus.OPEN)
                .enabled(true)
                .notes("Lifecycle profile")
                .lastEvaluatedAt("2026-04-28T10:00:00Z")
                .nextRecalcDate("2026-04-29")
                .build();
    }
}