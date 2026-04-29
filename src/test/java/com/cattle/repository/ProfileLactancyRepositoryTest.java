package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.ProfileLactancy;
import com.cattle.enums.LogType;
import com.cattle.exceptions.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("repository")
class ProfileLactancyRepositoryTest {

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<ProfileLactancy> table;

    @Mock
    private DynamoDbIndex<ProfileLactancy> gsi1Index;

    @Mock
    private PageIterable<ProfileLactancy> indexPageIterable;

    @Mock
    private PageIterable<ProfileLactancy> tablePageIterable;

    @Mock
    private Page<ProfileLactancy> page;

    private ProfileLactancyRepository repository;

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(enhancedClient.table(any(), any(TableSchema.class))).thenReturn(table);
        repository = new ProfileLactancyRepository(lambdaContext, enhancedClient);
    }

    @Test
    void findById_existingItem_returnsOptional() {
        ArgumentCaptor<Key> captor = ArgumentCaptor.forClass(Key.class);
        ProfileLactancy expected = createProfileLactancy();
        when(table.getItem(captor.capture())).thenReturn(expected);

        Optional<ProfileLactancy> result = repository.findById("BOVINE#101", "LACT#01");

        assertTrue(result.isPresent());
        assertSame(expected, result.get());
        assertEquals("BOVINE#101", captor.getValue().partitionKeyValue().s());
        assertEquals("LACT#01", captor.getValue().sortKeyValue().orElseThrow().s());
    }

    @Test
    void findById_resourceNotFound_returnsEmptyOptional() {
        when(table.getItem(any(Key.class)))
                .thenThrow(ResourceNotFoundException.builder().message("missing table").build());

        Optional<ProfileLactancy> result = repository.findById("BOVINE#101", "LACT#01");

        assertTrue(result.isEmpty());
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("ProfileLactancy table not found"), any(ResourceNotFoundException.class));
    }

    @Test
    void findAll_withResults_returnsOptionalList() {
        List<ProfileLactancy> items = List.of(createProfileLactancy(), createProfileLactancy("BOVINE#102", "LACT#02"));
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class))).thenReturn(indexPageIterable);
        when(indexPageIterable.iterator()).thenReturn(List.of(page).iterator());
        when(page.items()).thenReturn(items);

        Optional<List<ProfileLactancy>> result = repository.findAll();

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        verify(lambdaContext).logInfo(eq(LogType.REPOSITORY), eq("findAll: 2 records found in table: null"));
    }

    @Test
    void findAll_withoutResults_returnsEmptyOptional() {
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class))).thenReturn(indexPageIterable);
        when(indexPageIterable.iterator()).thenReturn(List.of(page).iterator());
        when(page.items()).thenReturn(Collections.emptyList());

        Optional<List<ProfileLactancy>> result = repository.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_dynamoDbException_throwsRepositoryException() {
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class)))
                .thenThrow(DynamoDbException.builder().message("query failed").build());

        assertThrows(RepositoryException.class, repository::findAll);

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error finding lactancies"), any(DynamoDbException.class));
    }

    @Test
    void save_validEntity_returnsSameEntity() {
        ProfileLactancy entity = createProfileLactancy();
        doNothing().when(table).putItem(entity);

        Optional<ProfileLactancy> result = repository.save(entity);

        assertTrue(result.isPresent());
        assertSame(entity, result.get());
    }

    @Test
    void save_dynamoDbException_throwsRepositoryException() {
        ProfileLactancy entity = createProfileLactancy();
        doThrow(DynamoDbException.builder().message("save failed").build()).when(table).putItem(entity);

        assertThrows(RepositoryException.class, () -> repository.save(entity));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error saving ProfileLactancy"), any(DynamoDbException.class));
    }

    @Test
    void update_validEntity_returnsSameEntity() {
        ProfileLactancy entity = createProfileLactancy();
        doNothing().when(table).putItem(entity);

        Optional<ProfileLactancy> result = repository.update(entity);

        assertTrue(result.isPresent());
        assertSame(entity, result.get());
    }

    @Test
    void update_dynamoDbException_throwsRepositoryException() {
        ProfileLactancy entity = createProfileLactancy();
        doThrow(DynamoDbException.builder().message("update failed").build()).when(table).putItem(entity);

        assertThrows(RepositoryException.class, () -> repository.update(entity));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error updating ProfileLactancy"), any(DynamoDbException.class));
    }

    @Test
    void deleteById_buildsKeyCorrectly() {
        ArgumentCaptor<Key> captor = ArgumentCaptor.forClass(Key.class);

        repository.deleteById("BOVINE#101", "LACT#01");

        verify(table).deleteItem(captor.capture());
        assertEquals("BOVINE#101", captor.getValue().partitionKeyValue().s());
        assertEquals("LACT#01", captor.getValue().sortKeyValue().orElseThrow().s());
    }

    @Test
    void deleteById_exception_throwsRepositoryException() {
        doThrow(new RuntimeException("delete failed")).when(table).deleteItem(any(Key.class));

        assertThrows(RepositoryException.class, () -> repository.deleteById("BOVINE#101", "LACT#01"));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error deleting ProfileLactancy by Id"), any(RuntimeException.class));
    }

    @Test
    void findAllLactations_withResults_returnsOptionalList() {
        List<ProfileLactancy> items = List.of(createProfileLactancy(), createProfileLactancy("BOVINE#102", "LACT#02"));
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class))).thenReturn(indexPageIterable);
        doAnswer(invocation -> {
            java.util.function.Consumer<Page<ProfileLactancy>> consumer = invocation.getArgument(0);
            consumer.accept(page);
            return null;
        }).when(indexPageIterable).forEach(any());
        when(page.items()).thenReturn(items);

        Optional<List<ProfileLactancy>> result = repository.findAllLactations("001");

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        verify(lambdaContext).logInfo(eq(LogType.REPOSITORY), eq("findAllLactations: 2 records found"));
    }

    @Test
    void findAllLactations_withoutResults_returnsEmptyOptional() {
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class))).thenReturn(indexPageIterable);
        doAnswer(invocation -> null).when(indexPageIterable).forEach(any());

        Optional<List<ProfileLactancy>> result = repository.findAllLactations("001");

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllLactations_dynamoDbException_throwsRepositoryException() {
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class)))
                .thenThrow(DynamoDbException.builder().message("query failed").build());

        assertThrows(RepositoryException.class, () -> repository.findAllLactations("001"));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error finding all lactations"), any(DynamoDbException.class));
    }

    @Test
    void findAllLactationsByBovine_withResults_returnsOptionalList() {
        List<ProfileLactancy> items = List.of(createProfileLactancy(), createProfileLactancy("BOVINE#101", "LACT#02"));
        when(table.query(any(java.util.function.Consumer.class))).thenReturn(tablePageIterable);
        doAnswer(invocation -> {
            java.util.function.Consumer<Page<ProfileLactancy>> consumer = invocation.getArgument(0);
            consumer.accept(page);
            return null;
        }).when(tablePageIterable).forEach(any());
        when(page.items()).thenReturn(items);

        Optional<List<ProfileLactancy>> result = repository.findAllLactationsByBovine("BOVINE#101");

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        verify(lambdaContext).logInfo(eq(LogType.REPOSITORY), eq("findAllLactationsByBovine: 2 lactations for BOVINE#101"));
    }

    @Test
    void findAllLactationsByBovine_withoutResults_returnsEmptyOptional() {
        when(table.query(any(java.util.function.Consumer.class))).thenReturn(tablePageIterable);
        doAnswer(invocation -> null).when(tablePageIterable).forEach(any());

        Optional<List<ProfileLactancy>> result = repository.findAllLactationsByBovine("BOVINE#101");

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllLactationsByBovine_dynamoDbException_throwsRepositoryException() {
        when(table.query(any(java.util.function.Consumer.class)))
                .thenThrow(DynamoDbException.builder().message("query failed").build());

        assertThrows(RepositoryException.class,
                () -> repository.findAllLactationsByBovine("BOVINE#101"));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error finding lactations by bovine"), any(DynamoDbException.class));
    }

    private ProfileLactancy createProfileLactancy() {
        return createProfileLactancy("BOVINE#101", "LACT#01");
    }

    private ProfileLactancy createProfileLactancy(String pk, String sk) {
        return ProfileLactancy.builder()
                .pk(pk)
                .sk(sk)
                .gsi1pk("LACT#FARM#001")
                .gsi1sk(pk)
                .createdAt("2026-01-01")
                .dryDate("2026-10-10")
                .endDate(null)
                .lactationNumber("1")
                .notes("Lactancy profile")
                .startDate("2026-01-01")
                .status("OPEN")
                .build();
    }
}