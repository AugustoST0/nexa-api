package org.senai.config;

import org.senai.model.Colaborador;
import org.senai.model.Grupo;
import org.senai.model.Tag;
import org.senai.repositories.ColaboradorRepository;
import org.senai.repositories.GrupoRepository;
import org.senai.repositories.TagRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ApplicationScoped
public class TestConfig {

    @Inject
    TagRepository tagRepository;

    @Inject
    GrupoRepository grupoRepository;

    @Inject
    ColaboradorRepository colaboradorRepository;

    private final Random random = new Random();

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        System.out.println("Iniciando seed do banco de dados...");

        // Criar 50 Tags (verificando duplicatas)
        List<Tag> tags = createTags();
        System.out.println("Tags processadas (criadas ou já existentes)");

        // Criar 6 Grupos (pesquisas salvas) definidos por tokens (verificando duplicatas)
        createGrupos();
        System.out.println("Grupos processados (criados ou já existentes)");

        // Criar 60 Colaboradores com 5 tags cada (verificando duplicatas)
        createColaboradores(tags);
        System.out.println("Colaboradores processados (criados ou já existentes)");

        System.out.println("Seed concluído com sucesso!");
    }

    private List<Tag> createTags() {
        List<Tag> tags = new ArrayList<>();
        int tagsCriadas = 0;
        int tagsExistentes = 0;

        String[][] tagsData = {
            // Tecnologia e Desenvolvimento
            {"Java", "Linguagem de programação Java para desenvolvimento backend"},
            {"Python", "Linguagem de programação Python para desenvolvimento e análise de dados"},
            {"JavaScript", "Linguagem de programação JavaScript para desenvolvimento web"},
            {"React", "Framework React para desenvolvimento de interfaces"},
            {"Node.js", "Runtime Node.js para desenvolvimento backend"},
            {"SQL", "Linguagem SQL para manipulação de bancos de dados"},
            {"Git", "Sistema de controle de versão Git"},
            {"Docker", "Tecnologia de containerização Docker"},
            {"AWS", "Serviços de cloud computing da Amazon Web Services"},
            {"Kubernetes", "Orquestração de containers com Kubernetes"},

            // Gestão e Liderança
            {"Liderança de Equipe", "Habilidade de liderar e motivar equipes"},
            {"Gestão de Projetos", "Capacidade de gerenciar projetos do início ao fim"},
            {"Scrum", "Metodologia ágil Scrum"},
            {"Kanban", "Metodologia ágil Kanban"},
            {"PMP", "Certificação em Project Management Professional"},
            {"Negociação", "Habilidade de negociar acordos e contratos"},
            {"Tomada de Decisão", "Capacidade de tomar decisões estratégicas"},
            {"Planejamento Estratégico", "Habilidade de planejar estrategicamente"},
            {"Gestão de Pessoas", "Competência em gestão de recursos humanos"},
            {"Coaching", "Habilidade de desenvolver pessoas através de coaching"},

            // Comunicação e Relacionamento
            {"Comunicação Eficaz", "Habilidade de se comunicar de forma clara e objetiva"},
            {"Apresentações", "Capacidade de fazer apresentações impactantes"},
            {"Redação Técnica", "Habilidade de escrever documentação técnica"},
            {"Inglês Avançado", "Fluência em inglês para comunicação internacional"},
            {"Espanhol", "Fluência em espanhol"},
            {"Relacionamento Interpessoal", "Habilidade de construir relacionamentos"},
            {"Trabalho em Equipe", "Capacidade de trabalhar colaborativamente"},
            {"Mediação de Conflitos", "Habilidade de mediar conflitos"},
            {"Networking", "Capacidade de construir rede de contatos"},
            {"Atendimento ao Cliente", "Habilidade em atendimento e suporte ao cliente"},

            // Análise e Dados
            {"Análise de Dados", "Capacidade de analisar e interpretar dados"},
            {"Excel Avançado", "Domínio avançado do Microsoft Excel"},
            {"Power BI", "Ferramenta Power BI para visualização de dados"},
            {"Tableau", "Ferramenta Tableau para análise de dados"},
            {"Estatística", "Conhecimento em estatística e análise estatística"},
            {"Machine Learning", "Conhecimento em machine learning e IA"},
            {"Data Science", "Conhecimento em ciência de dados"},
            {"Business Intelligence", "Conhecimento em Business Intelligence"},
            {"Análise Financeira", "Capacidade de analisar dados financeiros"},
            {"Relatórios Gerenciais", "Habilidade de criar relatórios gerenciais"},

            // Design e Criatividade
            {"UI/UX Design", "Design de interfaces e experiência do usuário"},
            {"Photoshop", "Domínio do Adobe Photoshop"},
            {"Illustrator", "Domínio do Adobe Illustrator"},
            {"Figma", "Ferramenta Figma para design"},
            {"Design Thinking", "Metodologia Design Thinking"},
            {"Criatividade", "Habilidade criativa e inovadora"},
            {"Branding", "Conhecimento em branding e identidade visual"},
            {"Marketing Digital", "Conhecimento em marketing digital"},
            {"SEO", "Otimização para mecanismos de busca"},
            {"Redes Sociais", "Gestão de redes sociais e mídias sociais"}
        };

        for (String[] tagData : tagsData) {
            // Verificar se a tag já existe
            Tag tagExistente = tagRepository.findByNome(tagData[0]).orElse(null);
            
            if (tagExistente != null) {
                tags.add(tagExistente);
                tagsExistentes++;
            } else {
                Tag tag = new Tag();
                tag.setNome(tagData[0]);
                tag.setDescricao(tagData[1]);
                tagRepository.persist(tag);
                tags.add(tag);
                tagsCriadas++;
            }
        }

        System.out.println("Tags: " + tagsCriadas + " criadas, " + tagsExistentes + " já existiam");
        return tags;
    }

    private void createGrupos() {
        int gruposCriados = 0;
        int gruposExistentes = 0;

        // Grupos = pesquisas salvas definidas por tokens (tag + operador: E / OU / NÃO POSSUI)
        List<List<String>> gruposTokens = List.of(
            List.of("Java", "E", "Python"),
            List.of("React", "OU", "JavaScript"),
            List.of("Docker", "E", "Kubernetes", "NÃO POSSUI", "AWS"),
            List.of("Análise de Dados", "E", "Power BI"),
            List.of("Scrum", "OU", "Kanban"),
            List.of("Python", "E", "Machine Learning", "NÃO POSSUI", "Java")
        );

        for (List<String> tokens : gruposTokens) {
            String nome = String.join(" ", tokens);

            // Verificar se o grupo já existe (pelo nome)
            Grupo grupoExistente = grupoRepository.findByNome(nome).orElse(null);

            if (grupoExistente == null) {
                Grupo grupo = new Grupo();
                grupo.setNome(nome);
                grupo.setTokens(new ArrayList<>(tokens));
                grupoRepository.persist(grupo);
                gruposCriados++;
            } else {
                gruposExistentes++;
            }
        }

        System.out.println("Grupos: " + gruposCriados + " criados, " + gruposExistentes + " já existiam");
    }

    private void createColaboradores(List<Tag> tags) {
        int colaboradoresCriados = 0;
        int colaboradoresExistentes = 0;
        
        // Arrays de dados para gerar 60 colaboradores
        String[] nomes = {
            "Ana Silva", "Carlos Santos", "Maria Oliveira", "João Pereira", "Fernanda Costa",
            "Ricardo Alves", "Juliana Lima", "Roberto Martins", "Patricia Souza", "Lucas Ferreira",
            "Amanda Rocha", "Bruno Carvalho", "Camila Dias", "Diego Ramos", "Larissa Gomes",
            "Thiago Barbosa", "Renata Araujo", "Felipe Mendes", "Gabriela Nunes", "Rodrigo Castro",
            "Beatriz Lopes", "Marcelo Rocha", "Isabela Santos", "André Lima", "Carolina Freitas",
            "Rafael Moreira", "Vanessa Almeida", "Gustavo Ribeiro", "Mariana Correia", "Paulo Henrique",
            "Luciana Martins", "Eduardo Silva", "Tatiana Costa", "Fabio Oliveira", "Juliana Rodrigues",
            "Leonardo Souza", "Priscila Araujo", "Henrique Nunes", "Daniela Castro", "Marcos Lopes",
            "Fernanda Rocha", "Vinicius Santos", "Bianca Lima", "Alexandre Freitas", "Monique Moreira",
            "Rafaela Almeida", "Guilherme Ribeiro", "Sabrina Correia", "Felipe Martins", "Leticia Silva",
            "Rodrigo Costa", "Amanda Oliveira", "Thiago Rodrigues", "Carla Souza", "Bruno Araujo",
            "Patricia Nunes", "Ricardo Castro", "Juliana Lopes", "Diego Rocha", "Mariana Santos"
        };

        String[] cargos = {
            "Desenvolvedor Junior", "Desenvolvedor Pleno", "Desenvolvedor Senior",
            "Arquiteto de Software", "Tech Lead", "Gerente de Projetos",
            "Product Manager", "Scrum Master", "Analista de Dados",
            "Analista de Negócios", "Especialista em BI", "DevOps Engineer",
            "Designer UX/UI", "Gerente de Marketing", "Coordenador de RH",
            "Analista de Qualidade", "Especialista em Segurança", "Analista de Sistemas"
        };

        String[] departamentos = {
            "TI", "Gestão", "Analytics", "Design", "Marketing", "RH", "Qualidade", "Produto", "Negócios"
        };

        // Tags por perfil (distribuídas entre os colaboradores)
        String[][] tagsPorPerfil = {
            {"Java", "SQL", "Git", "Scrum", "Liderança de Equipe"},
            {"Gestão de Projetos", "PMP", "Comunicação Eficaz", "Negociação", "Planejamento Estratégico"},
            {"Análise de Dados", "Excel Avançado", "Power BI", "SQL", "Estatística"},
            {"Java", "Python", "AWS", "Docker", "Planejamento Estratégico"},
            {"UI/UX Design", "Figma", "Design Thinking", "Criatividade", "Comunicação Eficaz"},
            {"Docker", "Kubernetes", "AWS", "Git", "Python"},
            {"Gestão de Projetos", "Scrum", "Comunicação Eficaz", "Análise de Dados", "Negociação"},
            {"Análise de Dados", "Comunicação Eficaz", "Apresentações", "Excel Avançado", "Negociação"},
            {"Scrum", "Kanban", "Liderança de Equipe", "Comunicação Eficaz", "Mediação de Conflitos"},
            {"JavaScript", "React", "Node.js", "SQL", "Git"},
            {"Marketing Digital", "Redes Sociais", "SEO", "Comunicação Eficaz", "Análise de Dados"},
            {"Análise de Dados", "Excel Avançado", "Comunicação Eficaz", "Trabalho em Equipe", "SQL"},
            {"Gestão de Pessoas", "Comunicação Eficaz", "Coaching", "Relacionamento Interpessoal", "Apresentações"},
            {"Business Intelligence", "Power BI", "SQL", "Análise de Dados", "Estatística"},
            {"JavaScript", "React", "Git", "Scrum", "UI/UX Design"},
            {"Python", "SQL", "Machine Learning", "Análise de Dados", "Estatística"},
            {"Java", "SQL", "Git", "Docker", "Scrum"},
            {"React", "JavaScript", "Git", "Scrum", "Trabalho em Equipe"},
            {"Gestão de Projetos", "Comunicação Eficaz", "Negociação", "Apresentações", "Planejamento Estratégico"},
            {"AWS", "Docker", "Kubernetes", "Git", "Python"}
        };

        for (int i = 0; i < 60; i++) {
            String matricula = "MAT" + String.format("%03d", i + 1);
            String emailBase = nomes[i].toLowerCase().replace(" ", ".").replace("ã", "a").replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u");
            String email = (i > 0 && emailBase.equals(nomes[i-1].toLowerCase().replace(" ", "."))) ? emailBase + i + "@empresa.com" : emailBase + "@empresa.com";
            String cpf = generateCpf(i);
            
            // Verificar se o colaborador já existe (por matrícula, email ou CPF)
            boolean existe = colaboradorRepository.findByMatricula(matricula).isPresent() ||
                           colaboradorRepository.findByEmail(email).isPresent() ||
                           colaboradorRepository.findByCpf(cpf).isPresent();
            
            if (!existe) {
                Colaborador colaborador = new Colaborador();
                colaborador.setNome(nomes[i]);
                colaborador.setMatricula(matricula);
                colaborador.setEmail(email);
                colaborador.setCpf(cpf);
                
                // Data de nascimento entre 1980 e 2000
                int anoNascimento = 1980 + (i % 21);
                int mesNascimento = 1 + (i % 12);
                int diaNascimento = 1 + (i % 28);
                colaborador.setDataNascimento(LocalDate.of(anoNascimento, mesNascimento, diaNascimento));
                
                // Data de admissão entre 2018 e 2023
                int anoAdmissao = 2018 + (i % 6);
                int mesAdmissao = 1 + (i % 12);
                colaborador.setDataAdmissao(LocalDate.of(anoAdmissao, mesAdmissao, 1 + (i % 15)));
                
                colaborador.setCargo(cargos[i % cargos.length]);
                colaborador.setDepartamento(departamentos[i % departamentos.length]);

                // Adicionar 5 tags ao colaborador (usando padrão circular dos perfis)
                List<Tag> tagsDoColaborador = new ArrayList<>();
                String[] tagsPerfil = tagsPorPerfil[i % tagsPorPerfil.length];
                for (String nomeTag : tagsPerfil) {
                    Tag tag = tags.stream()
                        .filter(t -> t.getNome().equals(nomeTag))
                        .findFirst()
                        .orElse(null);
                    if (tag != null) {
                        tagsDoColaborador.add(tag);
                    }
                }
                colaborador.setTags(tagsDoColaborador);

                colaboradorRepository.persist(colaborador);
                colaboradoresCriados++;
            } else {
                colaboradoresExistentes++;
            }
        }
        
        System.out.println("Colaboradores: " + colaboradoresCriados + " criados, " + colaboradoresExistentes + " já existiam");
    }

    private String generateCpf(int index) {
        // Gera CPF único baseado no índice (formato: XXX.XXX.XXX-XX)
        int base = 100000000 + index;
        String cpfBase = String.format("%09d", base);
        int digito1 = (index % 10);
        int digito2 = ((index / 10) % 10);
        return cpfBase.substring(0, 3) + "." + cpfBase.substring(3, 6) + "." + cpfBase.substring(6, 9) + "-" + String.format("%02d", digito1 * 10 + digito2);
    }
}

