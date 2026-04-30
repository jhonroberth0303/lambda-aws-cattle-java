package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.ProfileReproductive;
import com.cattle.enums.LogType;
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
class ProfileReproductiveRepositoryTest {

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<ProfileReproductive> table;

    @Mock
    private PageIterable<ProfileReproductive> pageIterable;

    @Mock
    private SdkIterable<ProfileReproductive> sdkIterable;

    private ProfileReproductiveRepository repository;

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(enhancedClient.table(any(), any(TableSchema.class))).thenReturn(table);
        repository = new ProfileReproductiveRepository(lambdaContext, enhancedClient);
    }

    @Test
    void findById_existingItem_returnsOptional() {
        ArgumentCaptor<Key> captor = ArgumentCaptor.forClass(Key.class);
        ProfileReproductive expected = createProfileReproductive();
        when(table.getItem(captor.capture())).thenReturn(expected);

        Optional<ProfileReproductive> result = repository.findById("BOVINE#101", "REPRO#CURRENT");

        assertTrue(result.isPresent());
        assertSame(expected, result.get());
        assertEquals("BOVINE#101", captor.getValue().partitionKeyValue().s());
        assertEquals("REPRO#CURRENT", captor.getValue().sortKeyValue().orElseThrow().s());
    }

    @Test
    void findById_resourceNotFound_returnsEmptyOptional() {
        when(table.getItem(any(Key.class)))
                .thenThrow(ResourceNotFoundException.builder().message("missing table").build());

        Optional<ProfileReproductive> result = repository.findById("BOVINE#101", "REPRO#CURRENT");

        assertTrue(result.isEmpty());
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("ProfileReproductive table not found"), any(ResourceNotFoundException.class));
    }

    @Test
    void findById_unexpectedException_throwsRepositoryException() {
        when(table.getItem(any(Key.class))).thenThrow(new RuntimeException("boom"));

        assertThrows(RepositoryException.class,
                () -> repository.findById("BOVINE#101", "REPRO#CURRENT"));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("findById error"), any(RuntimeException.class));
    }

    @Test
    void findAll_withResults_returnsList() {
        List<ProfileReproductive> items = List.of(createProfileReproductive(), createProfileReproductive("BOVINE#102"));
        when(table.scan()).thenReturn(pageIterable);
        when(pageIterable.items()).thenReturn(sdkIterable);
        doAnswer(invocation -> {
            java.util.function.Consumer<ProfileReproductive> consumer = invocation.getArgument(0);
            items.forEach(consumer);
            return null;
        }).when(sdkIterable).forEach(any());

        List<ProfileReproductive> result = repository.findAll();

        assertEquals(2, result.size());
        assertEquals("BOVINE#101", result.get(0).getPk());
        assertEquals("BOVINE#102", result.get(1).getPk());
    }

    @Test
    void findAll_dynamoDbException_throwsRepositoryException() {
        when(table.scan()).thenThrow(DynamoDbException.builder().message("scan failed").build());

        assertThrows(RepositoryException.class, repository::findAll);

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error finding all ProfileReproductive"), any(DynamoDbException.class));
    }

    @Test
    void save_validEntity_returnsSameEntity() {
        ProfileReproductive entity = createProfileReproductive();
        doNothing().when(table).putItem(entity);

        Optional<ProfileReproductive> result = repository.save(entity);

        assertTrue(result.isPresent());
        assertSame(entity, result.get());
        verify(table).putItem(entity);
    }

    @Test
    void save_dynamoDbException_throwsRepositoryException() {
        ProfileReproductive entity = createProfileReproductive();
        doThrow(DynamoDbException.builder().message("save failed").build()).when(table).putItem(entity);

        assertThrows(RepositoryException.class, () -> repository.save(entity));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error saving ProfileReproductive"), any(DynamoDbException.class));
    }

    @Test
    void update_validEntity_returnsSameEntity() {
        ProfileReproductive entity = createProfileReproductive();
        doNothing().when(table).putItem(entity);

        Optional<ProfileReproductive> result = repository.update(entity);

        assertTrue(result.isPresent());
        assertSame(entity, result.get());
    }

    @Test
    void deleteById_buildsKeyCorrectly() {
        ArgumentCaptor<Key> captor = ArgumentCaptor.forClass(Key.class);

        repository.deleteById("BOVINE#101", "REPRO#CURRENT");

        verify(table).deleteItem(captor.capture());
        assertEquals("BOVINE#101", captor.getValue().partitionKeyValue().s());
        assertEquals("REPRO#CURRENT", captor.getValue().sortKeyValue().orElseThrow().s());
    }

    @Test
    void deleteById_exception_throwsRepositoryException() {
        doThrow(new RuntimeException("delete failed")).when(table).deleteItem(any(Key.class));

        assertThrows(RepositoryException.class,
                () -> repository.deleteById("BOVINE#101", "REPRO#CURRENT"));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error deleting ProfileReproductive by Id"), any(RuntimeException.class));
    }

    private ProfileReproductive createProfileReproductive() {
        return createProfileReproductive("BOVINE#101");
    }

    private ProfileReproductive createProfileReproductive(String pk) {
        return ProfileReproductive.builder()
                .pk(pk)
                .sk("REPRO#CURRENT")
                .gsi1pk("REPRO")
                .gsi1sk(pk)
                .currentLactationId("LACT#01")
                .currentPregnancyId("PREG#ACTIVE")
                .updatedAt("2026-04-28T10:00:00Z")
                .build();
    }
}