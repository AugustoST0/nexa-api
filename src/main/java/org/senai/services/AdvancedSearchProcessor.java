package org.senai.services;

import org.senai.model.Tag;
import org.senai.repositories.TagRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class AdvancedSearchProcessor {

    @Inject
    TagRepository tagRepository;

    private static final String OPERATOR_AND = "E";
    private static final String OPERATOR_OR = "OU";
    private static final String OPERATOR_NOT = "NÃO POSSUI";

    /**
     * Parses and validates tokens from advanced search
     */
    public List<String> parseTokens(List<String> tokens) {
        if (tokens == null) {
            return new ArrayList<>();
        }
        
        return tokens.stream()
            .filter(token -> token != null && !token.trim().isEmpty())
            .map(this::sanitizeToken)
            .filter(token -> !token.isEmpty())
            .collect(Collectors.toList());
    }
    
    /**
     * Sanitizes a token to prevent injection attacks
     */
    private String sanitizeToken(String token) {
        if (token == null) {
            return "";
        }
        
        // Remove potentially dangerous characters and trim
        String sanitized = token.trim()
            .replaceAll("[<>\"';()&+\\\\]", "") // Remove common injection characters
            .replaceAll("\\s+", " "); // Normalize whitespace
        
        // Limit token length to prevent DoS attacks
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100);
        }
        
        return sanitized;
    }

    /**
     * Resolves a token to a list of tag names
     * If token is a tag name, returns it as a single-item list
     * If token is unknown, returns empty list
     */
    public List<String> resolveToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return new ArrayList<>();
        }

        token = token.trim();

        try {
            // Check if it's a tag
            Optional<Tag> tag = tagRepository.findByNome(token);
            if (tag.isPresent()) {
                String tagName = tag.get().getNome();
                return tagName != null ? List.of(tagName) : new ArrayList<>();
            }
        } catch (Exception e) {
            // Log error but don't fail the entire search
            System.err.println("Error resolving token '" + token + "': " + e.getMessage());
        }

        // Unknown token - return empty list
        return new ArrayList<>();
    }

    /**
     * Builds HQL query from tokens
     * Returns a map with "query" and "params" keys
     */
    public Map<String, Object> buildHQLQuery(List<String> tokens) {
        try {
            List<String> parsedTokens = parseTokens(tokens);
            
            // Validate tokens before processing
            validateTokens(parsedTokens);
            
            if (parsedTokens.isEmpty()) {
                return Map.of(
                    "query", "SELECT c FROM Colaborador c WHERE 1=1",
                    "params", new HashMap<String, Object>()
                );
            }

            // Find the NOT operator index
            int notIndex = -1;
            for (int i = 0; i < parsedTokens.size(); i++) {
                if (OPERATOR_NOT.equals(parsedTokens.get(i))) {
                    notIndex = i;
                    break;
                }
            }

            // Split into inclusion and exclusion parts
            List<String> inclusionTokens = notIndex == -1 
                ? parsedTokens 
                : parsedTokens.subList(0, notIndex);
            
            List<String> exclusionTokens = notIndex == -1 
                ? new ArrayList<>() 
                : parsedTokens.subList(notIndex + 1, parsedTokens.size());

            StringBuilder query = new StringBuilder("SELECT c FROM Colaborador c WHERE 1=1");
            Map<String, Object> params = new HashMap<>();
            int paramCounter = 0;

            // Process inclusion tokens
            if (!inclusionTokens.isEmpty()) {
                List<List<String>> blocks = splitByOperator(inclusionTokens, OPERATOR_AND);
                
                for (List<String> block : blocks) {
                    if (block.isEmpty()) continue;
                    
                    query.append(" AND (");
                    boolean hasValidCondition = false;
                    
                    List<List<String>> orGroups = splitByOperator(block, OPERATOR_OR);
                    for (int i = 0; i < orGroups.size(); i++) {
                        List<String> orGroup = orGroups.get(i);
                        if (orGroup.isEmpty()) continue;
                        
                        if (hasValidCondition) {
                            query.append(" OR ");
                        }
                        
                        boolean hasValidOrCondition = false;
                        for (int j = 0; j < orGroup.size(); j++) {
                            String token = orGroup.get(j);
                            List<String> resolvedTags = resolveToken(token);
                            
                            if (!resolvedTags.isEmpty()) {
                                if (hasValidOrCondition) {
                                    query.append(" OR ");
                                }
                                
                                for (int k = 0; k < resolvedTags.size(); k++) {
                                    String tagName = resolvedTags.get(k);
                                    if (k > 0) {
                                        query.append(" OR ");
                                    }
                                    String paramName = "tag" + paramCounter;
                                    query.append("EXISTS (SELECT t FROM Tag t WHERE t.nome = :").append(paramName).append(" AND t MEMBER OF c.tags)");
                                    params.put(paramName, tagName);
                                    paramCounter++;
                                }
                                hasValidOrCondition = true;
                            }
                        }
                        
                        if (hasValidOrCondition) {
                            hasValidCondition = true;
                        }
                    }
                    
                    if (hasValidCondition) {
                        query.append(")");
                    } else {
                        // Remove the " AND (" that was added
                        query.setLength(query.length() - 6);
                    }
                }
            }

            // Process exclusion tokens
            if (!exclusionTokens.isEmpty()) {
                for (String token : exclusionTokens) {
                    // Skip operators in exclusion section
                    if (OPERATOR_AND.equals(token) || OPERATOR_OR.equals(token)) {
                        continue;
                    }
                    
                    List<String> resolvedTags = resolveToken(token);
                    for (String tagName : resolvedTags) {
                        String paramName = "tag" + paramCounter;
                        query.append(" AND NOT EXISTS (SELECT t FROM Tag t WHERE t.nome = :").append(paramName).append(" AND t MEMBER OF c.tags)");
                        params.put(paramName, tagName);
                        paramCounter++;
                    }
                }
            }

            return Map.of(
                "query", query.toString(),
                "params", params
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao construir consulta de busca avançada: " + e.getMessage(), e);
        }
    }

    /**
     * Builds an HQL query combining the token-based filters with optional
     * supervisor and admission-date filters. Any argument may be null/empty.
     */
    public Map<String, Object> buildCombinedQuery(List<String> tokens, List<Long> supervisorIds,
                                                  LocalDate dataAdmissaoInicio, LocalDate dataAdmissaoFim) {
        String baseQuery;
        Map<String, Object> params;

        if (tokens != null && !tokens.isEmpty()) {
            Map<String, Object> base = buildHQLQuery(tokens);
            baseQuery = (String) base.get("query");
            @SuppressWarnings("unchecked")
            Map<String, Object> baseParams = (Map<String, Object>) base.get("params");
            params = new HashMap<>(baseParams);
        } else {
            baseQuery = "SELECT c FROM Colaborador c WHERE 1=1";
            params = new HashMap<>();
        }

        StringBuilder query = new StringBuilder(baseQuery);

        if (supervisorIds != null && !supervisorIds.isEmpty()) {
            query.append(" AND EXISTS (SELECT s FROM Supervisao s WHERE s.supervisionado.id = c.id")
                 .append(" AND s.supervisor.id IN :supervisorIds AND s.dataFim IS NULL)");
            params.put("supervisorIds", supervisorIds);
        }

        if (dataAdmissaoInicio != null) {
            query.append(" AND c.dataAdmissao >= :dataAdmissaoInicio");
            params.put("dataAdmissaoInicio", dataAdmissaoInicio);
        }

        if (dataAdmissaoFim != null) {
            query.append(" AND c.dataAdmissao <= :dataAdmissaoFim");
            params.put("dataAdmissaoFim", dataAdmissaoFim);
        }

        return Map.of("query", query.toString(), "params", params);
    }

    /**
     * Validates tokens for security and correctness
     */
    private void validateTokens(List<String> tokens) {
        if (tokens == null) {
            throw new IllegalArgumentException("Lista de tokens não pode ser nula");
        }
        
        if (tokens.size() > 50) {
            throw new IllegalArgumentException("Número máximo de tokens excedido (máximo: 50)");
        }
        
        // Check for malformed token sequences
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            
            // Check if token ends with an operator (invalid)
            if (i == tokens.size() - 1 && isOperator(token)) {
                throw new IllegalArgumentException("Busca não pode terminar com operador: " + token);
            }
            
            // Check for consecutive operators
            if (i < tokens.size() - 1 && isOperator(token) && isOperator(tokens.get(i + 1))) {
                throw new IllegalArgumentException("Operadores consecutivos não são permitidos: " + token + " " + tokens.get(i + 1));
            }
        }
    }
    
    /**
     * Checks if a token is an operator
     */
    private boolean isOperator(String token) {
        return OPERATOR_AND.equals(token) || OPERATOR_OR.equals(token) || OPERATOR_NOT.equals(token);
    }

    /**
     * Splits a list of tokens by a given operator
     */
    private List<List<String>> splitByOperator(List<String> tokens, String operator) {
        List<List<String>> result = new ArrayList<>();
        List<String> currentBlock = new ArrayList<>();

        for (String token : tokens) {
            if (operator.equals(token)) {
                if (!currentBlock.isEmpty()) {
                    result.add(new ArrayList<>(currentBlock));
                    currentBlock.clear();
                }
            } else if (!OPERATOR_NOT.equals(token)) {
                currentBlock.add(token);
            }
        }

        if (!currentBlock.isEmpty()) {
            result.add(currentBlock);
        }

        return result.isEmpty() ? List.of(new ArrayList<>()) : result;
    }

    /**
     * Filters tags to show only those relevant to the search
     */
    public List<String> getRelevantTagNames(List<String> tokens) {
        List<String> parsedTokens = parseTokens(tokens);
        Set<String> relevantTags = new HashSet<>();

        // Find the NOT operator index
        int notIndex = -1;
        for (int i = 0; i < parsedTokens.size(); i++) {
            if (OPERATOR_NOT.equals(parsedTokens.get(i))) {
                notIndex = i;
                break;
            }
        }

        // Only process inclusion tokens for relevant tags
        List<String> inclusionTokens = notIndex == -1 
            ? parsedTokens 
            : parsedTokens.subList(0, notIndex);

        for (String token : inclusionTokens) {
            if (!OPERATOR_AND.equals(token) && !OPERATOR_OR.equals(token)) {
                List<String> resolved = resolveToken(token);
                relevantTags.addAll(resolved);
            }
        }

        return new ArrayList<>(relevantTags);
    }
}
