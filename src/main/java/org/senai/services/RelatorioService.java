package org.senai.services;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.senai.dtos.RelatorioResponseDTO;
import org.senai.exception.exceptions.BusinessRuleException;
import org.senai.exception.exceptions.RegisterNotFoundException;
import org.senai.model.Colaborador;
import org.senai.model.Grupo;
import org.senai.model.Relatorio;
import org.senai.repositories.ColaboradorRepository;
import org.senai.repositories.RelatorioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class RelatorioService {

    @Inject
    RelatorioRepository relatorioRepository;

    @Inject
    GrupoService grupoService;

    @Inject
    AdvancedSearchProcessor advancedSearchProcessor;

    @Inject
    ColaboradorRepository colaboradorRepository;

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] CABECALHO = {
            "id", "nome", "matricula", "email", "cpf", "cargo", "departamento", "dataAdmissao"
    };

    @Transactional
    public RelatorioResponseDTO gerar(Long grupoId) {
        Grupo grupo = grupoService.getById(grupoId);

        if (grupo.getTokens() == null || grupo.getTokens().isEmpty()) {
            throw new BusinessRuleException("O grupo não possui tokens de busca definidos");
        }

        Map<String, Object> queryMap = advancedSearchProcessor.buildHQLQuery(grupo.getTokens());
        String query = (String) queryMap.get("query");
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) queryMap.get("params");

        List<Colaborador> colaboradores = colaboradorRepository.searchAdvanced(query, params);

        Relatorio relatorio = new Relatorio();
        relatorio.setTitulo("Relatório - " + grupo.getNome() + " (" + relatorio.getGeradoEm().format(DATA) + ")");
        relatorio.setParametros(String.join(" ", grupo.getTokens()));
        relatorio.setGrupo(grupo);
        relatorio.setColaboradores(colaboradores);

        relatorioRepository.save(relatorio);

        return RelatorioResponseDTO.fromEntity(relatorio);
    }

    public List<RelatorioResponseDTO> getAll() {
        return relatorioRepository.find("ORDER BY geradoEm DESC").list().stream()
                .map(RelatorioResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public Relatorio getById(Long id) {
        Relatorio relatorio = relatorioRepository.findById(id);

        if (relatorio == null) {
            throw new RegisterNotFoundException("Relatório não encontrado");
        }

        relatorio.getColaboradores().size();
        return relatorio;
    }

    @Transactional
    public String downloadCsv(Long id) {
        Relatorio relatorio = getById(id);

        StringBuilder csv = new StringBuilder();
        csv.append(String.join(",", CABECALHO)).append("\n");

        for (Colaborador c : relatorio.getColaboradores()) {
            csv.append(campo(c.getId()))
                    .append(",").append(campo(c.getNome()))
                    .append(",").append(campo(c.getMatricula()))
                    .append(",").append(campo(c.getEmail()))
                    .append(",").append(campo(c.getCpf()))
                    .append(",").append(campo(c.getCargo()))
                    .append(",").append(campo(c.getDepartamento()))
                    .append(",").append(campo(c.getDataAdmissao()))
                    .append("\n");
        }

        return csv.toString();
    }

    @Transactional
    public byte[] downloadPdf(Long id) {
        Relatorio relatorio = getById(id);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, baos);
        document.open();

        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font cabecalhoFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font celulaFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

        document.add(new Paragraph(texto(relatorio.getTitulo()), tituloFont));
        document.add(new Paragraph("Gerado em: " + relatorio.getGeradoEm().format(DATA_HORA), normalFont));
        document.add(new Paragraph("Parâmetros: " + texto(relatorio.getParametros()), normalFont));
        document.add(new Paragraph(" ", normalFont));

        PdfPTable table = new PdfPTable(CABECALHO.length);
        table.setWidthPercentage(100);

        for (String coluna : CABECALHO) {
            PdfPCell cell = new PdfPCell(new Paragraph(coluna, cabecalhoFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        for (Colaborador c : relatorio.getColaboradores()) {
            table.addCell(new Paragraph(texto(c.getId()), celulaFont));
            table.addCell(new Paragraph(texto(c.getNome()), celulaFont));
            table.addCell(new Paragraph(texto(c.getMatricula()), celulaFont));
            table.addCell(new Paragraph(texto(c.getEmail()), celulaFont));
            table.addCell(new Paragraph(texto(c.getCpf()), celulaFont));
            table.addCell(new Paragraph(texto(c.getCargo()), celulaFont));
            table.addCell(new Paragraph(texto(c.getDepartamento()), celulaFont));
            table.addCell(new Paragraph(texto(c.getDataAdmissao()), celulaFont));
        }

        document.add(table);
        document.close();

        return baos.toByteArray();
    }

    private String campo(Object valor) {
        String texto = texto(valor);
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }

    private String texto(Object valor) {
        return valor != null ? valor.toString() : "";
    }
}
