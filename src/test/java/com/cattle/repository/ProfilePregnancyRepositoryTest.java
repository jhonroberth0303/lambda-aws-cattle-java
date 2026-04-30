package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.ProfilePregnancy;
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
class ProfilePregnancyRepositoryTest {

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<ProfilePregnancy> table;

    @Mock
    private PageIterable<ProfilePregnancy> pageIterable;

    @Mock
    private SdkIterable<ProfilePregnancy> sdkIterable;

    private ProfilePregnancyRepository repository;

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(enhancedClient.table(any(), any(TableSchema.class))).thenReturn(table);
        repository = new ProfilePregnancyRepository(lambdaContext, enhancedClient);
    }

    @Test
    void findById_existingItem_returnsOptional() {
        ArgumentCaptor<Key> captor = ArgumentCaptor.forClass(Key.class);
        ProfilePregnancy expected = createProfilePregnancy();
        when(table.getItem(captor.capture())).thenReturn(expected);

        Optional<ProfilePregnancy> result = repository.findById("BOVINE#101", "PREG#ACTIVE");

        assertTrue(result.isPresent());
        assertSame(expected, result.get());
        assertEquals("BOVINE#101", captor.getValue().partitionKeyValue().s());
        assertEquals("PREG#ACTIVE", captor.getValue().sortKeyValue().orElseThrow().s());
    }

    @Test
    void findById_resourceNotFound_returnsEmptyOptional() {
        when(table.getItem(any(Key.class)))
                .thenThrow(ResourceNotFoundException.builder().message("missing table").build());

        Optional<ProfilePregnancy> result = repository.findById("BOVINE#101", "PREG#ACTIVE");

        assertTrue(result.isEmpty());
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("ProfilePregnancy table not found"), any(ResourceNotFoundException.class));
    }

    @Test
    void findById_unexpectedException_throwsRepositoryException() {
        when(table.getItem(any(Key.class))).thenThrow(new RuntimeException("boom"));

        assertThrows(RepositoryException.class,
                () -> repository.findById("BOVINE#101", "PREG#ACTIVE"));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("findById error"), any(RuntimeException.class));
    }

    @Test
    void findAll_withResults_returnsList() {
        List<ProfilePregnancy> items = List.of(createProfilePregnancy(), createProfilePregnancy("BOVINE#102"));
        when(table.scan()).thenReturn(pageIterable);
        when(pageIterable.items()).thenReturn(sdkIterable);
        doAnswer(invocation -> {
            java.util.function.Consumer<ProfilePregnancy> consumer = invocation.getArgument(0);
            items.forEach(consumer);
            return null;
        }).when(sdkIterable).forEach(any());

        List<ProfilePregnancy> result = repository.findAll();

        assertEquals(2, result.size());
        assertEquals("BOVINE#101", result.get(0).getPk());
        assertEquals("BOVINE#102", result.get(1).getPk());
    }

    @Test
    void findAll_dynamoDbException_throwsRepositoryException() {
        when(table.scan()).thenThrow(DynamoDbException.builder().message("scan failed").build());

        assertThrows(RepositoryException.class, repository::findAll);

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error finding all ProfilePregnancy"), any(DynamoDbException.class));
    }

    @Test
    void save_validEntity_returnsSameEntity() {
        ProfilePregnancy entity = createProfilePregnancy();
        doNothing().when(table).putItem(entity);

        Optional<ProfilePregnancy> result = repository.save(entity);

        assertTrue(result.isPresent());
        assertSame(entity, result.get());
        verify(table).putItem(entity);
    }

    @Test
    void save_dynamoDbException_throwsRepositoryException() {
        ProfilePregnancy entity = createProfilePregnancy();
        doThrow(DynamoDbException.builder().message("save failed").build()).when(table).putItem(entity);

        assertThrows(RepositoryException.class, () -> repository.save(entity));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error saving ProfilePregnancy"), any(DynamoDbException.class));
    }

    @Test
    void update_validEntity_returnsSameEntity() {
        ProfilePregnancy entity = createProfilePregnancy();
        doNothing().when(table).putItem(entity);

        Optional<ProfilePregnancy> result = repository.update(entity);

        assertTrue(result.isPresent());
        assertSame(entity, result.get());
    }

    @Test
    void deleteById_buildsKeyCorrectly() {
        ArgumentCaptor<Key> captor = ArgumentCaptor.forClass(Key.class);

        repository.deleteById("BOVINE#101", "PREG#ACTIVE");

        verify(table).deleteItem(captor.capture());
        assertEquals("BOVINE#101", captor.getValue().partitionKeyValue().s());
        assertEquals("PREG#ACTIVE", captor.getValue().sortKeyValue().orElseThrow().s());
    }

    @Test
    void deleteById_exception_throwsRepositoryException() {
        doThrow(new RuntimeException("delete failed")).when(table).deleteItem(any(Key.class));

        assertThrows(RepositoryException.class,
                () -> repository.deleteById("BOVINE#101", "PREG#ACTIVE"));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error deleting ProfilePregnancy by Id"), any(RuntimeException.class));
    }

    private ProfilePregnancy createProfilePregnancy() {
        return createProfilePregnancy("BOVINE#101");
    }

    private ProfilePregnancy createProfilePregnancy(String pk) {
        return ProfilePregnancy.builder()
                .pk(pk)
                .sk("PREG#ACTIVE")
                .gsi1pk("PREG")
                .gsi1sk(pk)
                .calvingDate(null)
                .confirmationMethod("palpation")
                .createdAt("2026-04-01")
                .expectedDueDate("2026-12-15")
                .notes("Pregnancy profile")
                .serviceDate("2026-03-20")
                .status("ACTIVE")
                .build();
    }
}