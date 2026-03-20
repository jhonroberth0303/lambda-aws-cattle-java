package com.cattle.builders;

import com.cattle.entities.Task;

public class TaskBuilder {
    private String pk;
    private String sk;
    private String gsi1pk;
    private String gsi1sk;
    private String taskId;
    private String dueDate;
    private String kind;
    private String pastureId;
    private String status;

    public static TaskBuilder create() { return new TaskBuilder(); }

    public TaskBuilder pk(String v) { this.pk = v; return this; }
    public TaskBuilder sk(String v) { this.sk = v; return this; }
    public TaskBuilder gsi1pk(String v) { this.gsi1pk = v; return this; }
    public TaskBuilder gsi1sk(String v) { this.gsi1sk = v; return this; }
    public TaskBuilder taskId(String v) { this.taskId = v; return this; }
    public TaskBuilder dueDate(String v) { this.dueDate = v; return this; }
    public TaskBuilder kind(String v) { this.kind = v; return this; }
    public TaskBuilder pastureId(String v) { this.pastureId = v; return this; }
    public TaskBuilder status(String v) { this.status = v; return this; }

    public TaskBuilder defaults() {
        if (status == null) status = "PENDIENTE";
        return this;
    }

    public Task build() {
        requireNonBlank(pk, "pk");
        requireNonBlank(sk, "sk");
        requireNonBlank(taskId, "taskId");
        requireNonBlank(dueDate, "dueDate");
        requireNonBlank(kind, "kind");
        requireNonBlank(pastureId, "pastureId");
        if (gsi1pk == null) gsi1pk = "farm#UNKNOWN#pasture#" + pastureId;
        if (gsi1sk == null) gsi1sk = dueDate;
        return Task.builder()
                .pk(pk).sk(sk)
                .gsi1pk(gsi1pk).gsi1sk(gsi1sk)
                .taskId(taskId)
                .dueDate(dueDate)
                .kind(kind)
                .pastureId(pastureId)
                .status(status)
                .build();
    }

    private static void requireNonBlank(String v, String field) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException("Campo requerido vacío: " + field);
        }
    }
}

