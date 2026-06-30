package org.senai.services;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.senai.dtos.ColaboradorCreateUpdateDTO;
import org.senai.dtos.ImportPreviewDTO;
import org.senai.dtos.LinhaImportDTO;
import org.senai.repositories.ColaboradorRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@ApplicationScoped
public class ImportacaoColaboradorService {

    private static final int MAX_LINHAS_PREVIEW = 500;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Inject
    ColaboradorRepository colaboradorRepository;

    @Inject
    ColaboradorService colaboradorService;

    public ImportPreviewDTO parsePreview(InputStream csvFile, char delimitador) throws IOException {
        byte[] bytes = csvFile.readAllBytes();

        String conteudo = decodificarConteudo(bytes);

        CSVParser csvParser = new CSVParserBuilder()
                .withSeparator(delimitador)
                .withIgnoreQuotations(false)
                .build();

        List<String> colunasDetectadas = new ArrayList<>();
        List<LinhaImportDTO> linhas = new ArrayList<>();

        try (CSVReader reader = new CSVReaderBuilder(new StringReader(conteudo))
                .withCSVParser(csvParser)
                .build()) {

            String[] header = reader.readNext();
            if (header == null) {
                return new ImportPreviewDTO(colunasDetectadas, linhas);
            }

            for (String col : header) {
                colunasDetectadas.add(col.trim());
            }

            String[] row;
            int numeroLinha = 1;
            while ((row = reader.readNext()) != null && numeroLinha <= MAX_LINHAS_PREVIEW) {
                Map<String, String> dados = new LinkedHashMap<>();
                for (int i = 0; i < colunasDetectadas.size(); i++) {
                    dados.put(colunasDetectadas.get(i), i < row.length ? row[i].trim() : "");
                }
                linhas.add(new LinhaImportDTO(numeroLinha, dados, new ArrayList<>()));
                numeroLinha++;
            }
        } catch (Exception e) {
            throw new IOException("Erro ao parsear CSV: " + e.getMessage(), e);
        }

        return new ImportPreviewDTO(colunasDetectadas, linhas);
    }

    public List<LinhaImportDTO> validarLinhas(List<Map<String, String>> linhasMapeadas) {
        List<LinhaImportDTO> resultado = new ArrayList<>();

        Set<String> matriculasVistas = new HashSet<>();
        Set<String> emailsVistos = new HashSet<>();
        Set<String> cpfsVistos = new HashSet<>();

        int numeroLinha = 1;
        for (Map<String, String> dados : linhasMapeadas) {
            List<String> erros = new ArrayList<>();

            String nome = dados.getOrDefault("nome", "").trim();
            String matricula = dados.getOrDefault("matricula", "").trim();
            String email = dados.getOrDefault("email", "").trim();
            String cpf = dados.getOrDefault("cpf", "").trim();
            String dataNascimentoStr = dados.getOrDefault("dataNascimento", "").trim();
            String dataAdmissaoStr = dados.getOrDefault("dataAdmissao", "").trim();
            String cargo = dados.getOrDefault("cargo", "").trim();
            String departamento = dados.getOrDefault("departamento", "").trim();

            if (nome.isEmpty()) erros.add("Nome é obrigatório");
            if (matricula.isEmpty()) erros.add("Matrícula é obrigatória");
            if (email.isEmpty()) erros.add("Email é obrigatório");
            if (cpf.isEmpty()) erros.add("CPF é obrigatório");
            if (dataNascimentoStr.isEmpty()) erros.add("Data de nascimento é obrigatória");
            if (dataAdmissaoStr.isEmpty()) erros.add("Data de admissão é obrigatória");
            if (cargo.isEmpty()) erros.add("Cargo é obrigatório");
            if (departamento.isEmpty()) erros.add("Departamento é obrigatório");

            if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
                erros.add("Email inválido");
            }

            String cpfNorm = cpf.replaceAll("[^0-9]", "");
            if (!cpf.isEmpty() && cpfNorm.length() != 11) {
                erros.add("CPF inválido");
                cpfNorm = null;
            }

            LocalDate dataNascimento = null;
            if (!dataNascimentoStr.isEmpty()) {
                try {
                    dataNascimento = LocalDate.parse(dataNascimentoStr, DATE_FORMATTER);
                    if (!dataNascimento.isBefore(LocalDate.now())) {
                        erros.add("Data de nascimento deve ser no passado");
                    }
                } catch (DateTimeParseException e) {
                    erros.add("Data de nascimento inválida (formato esperado: yyyy-MM-dd)");
                }
            }

            if (!dataAdmissaoStr.isEmpty()) {
                try {
                    LocalDate dataAdmissao = LocalDate.parse(dataAdmissaoStr, DATE_FORMATTER);
                    if (dataAdmissao.isAfter(LocalDate.now())) {
                        erros.add("Data de admissão não pode ser futura");
                    }
                } catch (DateTimeParseException e) {
                    erros.add("Data de admissão inválida (formato esperado: yyyy-MM-dd)");
                }
            }

            if (!matricula.isEmpty()) {
                if (!matriculasVistas.add(matricula.toLowerCase())) {
                    erros.add("Matrícula duplicada no arquivo");
                } else if (colaboradorRepository.existsByMatricula(matricula)) {
                    erros.add("Matrícula já cadastrada");
                }
            }

            if (!email.isEmpty() && EMAIL_PATTERN.matcher(email).matches()) {
                if (!emailsVistos.add(email.toLowerCase())) {
                    erros.add("Email duplicado no arquivo");
                } else if (colaboradorRepository.existsByEmail(email)) {
                    erros.add("Email já cadastrado");
                }
            }

            if (cpfNorm != null) {
                if (!cpfsVistos.add(cpfNorm)) {
                    erros.add("CPF duplicado no arquivo");
                } else {
                    String cpfFormatado = cpfNorm.substring(0, 3) + "." + cpfNorm.substring(3, 6) + "."
                            + cpfNorm.substring(6, 9) + "-" + cpfNorm.substring(9);
                    if (colaboradorRepository.existsByCpf(cpfNorm) || colaboradorRepository.existsByCpf(cpfFormatado)) {
                        erros.add("CPF já cadastrado");
                    }
                }
            }

            resultado.add(new LinhaImportDTO(numeroLinha, new LinkedHashMap<>(dados), erros));
            numeroLinha++;
        }

        return resultado;
    }

    @Transactional
    public Map<String, Object> importar(List<LinhaImportDTO> linhas) {
        List<LinhaImportDTO> validas = linhas.stream()
                .filter(l -> l.erros().isEmpty())
                .toList();

        List<LinhaImportDTO> ignoradas = linhas.stream()
                .filter(l -> !l.erros().isEmpty())
                .toList();

        int totalImportados = 0;
        for (LinhaImportDTO linha : validas) {
            Map<String, String> dados = linha.dados();

            String cpfNorm = dados.getOrDefault("cpf", "").replaceAll("[^0-9]", "");
            String cpfFormatado = cpfNorm.substring(0, 3) + "." + cpfNorm.substring(3, 6) + "."
                    + cpfNorm.substring(6, 9) + "-" + cpfNorm.substring(9);

            LocalDate dataNascimento = LocalDate.parse(dados.get("dataNascimento"), DATE_FORMATTER);
            LocalDate dataAdmissao = LocalDate.parse(dados.get("dataAdmissao"), DATE_FORMATTER);

            ColaboradorCreateUpdateDTO dto = new ColaboradorCreateUpdateDTO(
                    dados.get("nome"),
                    dados.get("matricula"),
                    dados.get("email"),
                    cpfFormatado,
                    dataNascimento,
                    dataAdmissao,
                    dados.get("cargo"),
                    dados.get("departamento"),
                    null
            );

            colaboradorService.create(dto);
            totalImportados++;
        }

        Map<String, Object> resumo = new HashMap<>();
        resumo.put("totalImportados", totalImportados);
        resumo.put("totalIgnorados", ignoradas.size());
        resumo.put("linhasIgnoradas", ignoradas);
        return resumo;
    }

    private String decodificarConteudo(byte[] bytes) {
        CharsetDecoder utf8Decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            return utf8Decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            return Charset.forName("Windows-1252").decode(ByteBuffer.wrap(bytes)).toString();
        }
    }
}
