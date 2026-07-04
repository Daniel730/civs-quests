package dev.daniel730.rpgserver.quest;

public enum QuestAcceptResult {
    SUCCESS("Quest aceita: "),
    NOT_FOUND("Quest não encontrada."),
    ALREADY_COMPLETE("Esta quest já foi concluída."),
    ALREADY_STARTED("Esta quest já está em andamento."),
    LOCKED("Pré-requisitos não atendidos."),
    NO_PERMISSION("Você não tem permissão para esta quest."),
    MAX_ACTIVE("Limite de quests ativas atingido."),
    ARCHETYPE_LOCKED("Você já escolheu outro caminho.");

    private final String message;

    QuestAcceptResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
