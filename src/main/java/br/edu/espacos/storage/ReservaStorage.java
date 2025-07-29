package br.edu.espacos.storage;

import br.edu.espacos.model.Reserva;
import br.edu.espacos.model.StatusReserva;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe responsável pela persistência de reservas em arquivo TXT
 */
public class ReservaStorage {
    private static final String ARQUIVO_RESERVAS_NOME = "reservas.txt";
    private static final String DATA_DIR_NOME = "data";
    private static final String FULL_PATH_DATA_DIR;
    private static final String FULL_PATH_ARQUIVO_RESERVAS;

    static {
        String baseDir = "";
        try {
            // Tenta obter o caminho do JAR ou do diretório de classes
            // Isso funciona bem quando executado de um JAR ou de uma IDE (target/classes)
            baseDir = Paths.get(ReservaStorage.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().toString();
        } catch (URISyntaxException e) {
            System.err.println("Erro ao obter o caminho base via URI: " + e.getMessage());
            // Fallback para user.dir se URISyntaxException ocorrer
            baseDir = System.getProperty("user.dir");
        } catch (SecurityException e) {
            System.err.println("Erro de segurança ao obter o caminho base: " + e.getMessage());
            // Fallback para user.dir se SecurityException ocorrer
            baseDir = System.getProperty("user.dir");
        }

        // Ajusta o projectRoot para ser o diretório 'sistema-espacos' se estiver em um subdiretório como 'target/classes'
        String projectRoot = baseDir;
        if (baseDir.contains("target" + File.separator + "classes")) {
            // Se estiver em target/classes, volta para a raiz do projeto (sistema-espacos)
            int index = baseDir.indexOf("target" + File.separator + "classes");
            if (index != -1) {
                projectRoot = baseDir.substring(0, index); // Remove "target/classes" do caminho
            }
        } else if (baseDir.contains("sistema-espacos")) {
            // Se estiver em um subdiretório de sistema-espacos, encontra a raiz
            int index = baseDir.lastIndexOf("sistema-espacos");
            if (index != -1) {
                projectRoot = baseDir.substring(0, index + "sistema-espacos".length());
            }
        }

        FULL_PATH_DATA_DIR = projectRoot + File.separator + DATA_DIR_NOME;
        FULL_PATH_ARQUIVO_RESERVAS = FULL_PATH_DATA_DIR + File.separator + ARQUIVO_RESERVAS_NOME;

        File dataDir = new File(FULL_PATH_DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs(); // Cria o diretório 'data' se não existir
        }
        File arquivo = new File(FULL_PATH_ARQUIVO_RESERVAS);
        if (!arquivo.exists()) {
            try {
                arquivo.createNewFile(); // Cria o arquivo se não existir
            } catch (IOException e) {
                System.err.println("Erro ao criar arquivo de reservas: " + e.getMessage());
            }
        }
    }

    /**
     * Salva uma reserva no arquivo (adiciona ao final)
     */
    public static void salvar(Reserva reserva) {
        List<Reserva> reservas = carregarTodas();
        reservas.removeIf(r -> r.getId().equals(reserva.getId())); // Remove se já existe (para atualização)
        reservas.add(reserva);
        salvarTodos(reservas);
    }

    /**
     * Carrega todas as reservas do arquivo
     */
    public static List<Reserva> carregarTodas() {
        List<Reserva> reservas = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FULL_PATH_ARQUIVO_RESERVAS))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (!linha.trim().isEmpty()) {
                    Reserva reserva = Reserva.fromFileString(linha);
                    if (reserva != null) {
                        reservas.add(reserva);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar reservas: " + e.getMessage());
        }

        return reservas;
    }

    /**
     * Busca uma reserva por ID
     */
    public static Reserva buscarPorId(String id) {
        List<Reserva> reservas = carregarTodas();
        return reservas.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Busca reservas por espaço
     */
    public static List<Reserva> buscarPorEspaco(String espacoId) {
        List<Reserva> reservas = carregarTodas();
        return reservas.stream()
                .filter(r -> r.getEspacoId().equals(espacoId))
                .collect(Collectors.toList());
    }

    /**
     * Busca reservas por usuário
     */
    public static List<Reserva> buscarReservasPorUsuario(String usuarioId) {
        List<Reserva> reservas = carregarTodas();
        return reservas.stream()
                .filter(r -> r.getUsuarioId().equals(usuarioId))
                .collect(Collectors.toList());
    }

    /**
     * Busca reservas ativas para um espaço em um período
     */
    public static List<Reserva> buscarReservasAtivasPorEspacoEPeriodo(String espacoId,
                                                                      LocalDateTime inicio,
                                                                      LocalDateTime fim) {
        List<Reserva> reservas = carregarTodas();
        return reservas.stream()
                .filter(r -> r.getEspacoId().equals(espacoId))
                .filter(r -> r.getStatus() == StatusReserva.CONFIRMADA || r.getStatus() == StatusReserva.PENDENTE)
                .filter(r -> r.getDataHoraInicio().isBefore(fim) && r.getDataHoraFim().isAfter(inicio))
                .collect(Collectors.toList());
    }

    /**
     * Verifica se há conflito de horário para uma nova reserva
     */
    public static boolean temConflito(Reserva novaReserva) {
        List<Reserva> reservasExistentes = buscarReservasAtivasPorEspacoEPeriodo(
                novaReserva.getEspacoId(),
                novaReserva.getDataHoraInicio(),
                novaReserva.getDataHoraFim()
        );

        return reservasExistentes.stream()
                .anyMatch(r -> !r.getId().equals(novaReserva.getId()) && r.temConflitoCom(novaReserva));
    }

    /**
     * Atualiza uma reserva no arquivo
     */
    public static void atualizar(Reserva reservaAtualizada) {
        List<Reserva> reservas = carregarTodas();

        // Remove a reserva antiga e adiciona a atualizada
        reservas.removeIf(r -> r.getId().equals(reservaAtualizada.getId()));
        reservas.add(reservaAtualizada);

        salvarTodos(reservas);
    }

    /**
     * Cancela uma reserva
     */
    public static void cancelar(String id) {
        System.out.println("ReservaStorage: Tentando cancelar reserva com ID: " + id);
        List<Reserva> reservas = carregarTodas();
        boolean removido = reservas.removeIf(r -> r.getId().equals(id));
        if (removido) {
            salvarTodos(reservas);
            System.out.println("ReservaStorage: Reserva ID " + id + " removida do arquivo.");
        } else {
            System.out.println("ReservaStorage: Reserva ID " + id + " não encontrada para cancelamento.");
        }
    }

    /**
     * Salva todas as reservas no arquivo (sobrescreve)
     */
    private static void salvarTodos(List<Reserva> reservas) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FULL_PATH_ARQUIVO_RESERVAS))) {
            for (Reserva reserva : reservas) {
                writer.write(reserva.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar todas as reservas: " + e.getMessage());
        }
    }

    /**
     * Gera um novo ID único para reserva
     */
    public static String gerarNovoId() {
        List<Reserva> reservas = carregarTodas();
        int maiorId = 0;

        for (Reserva reserva : reservas) {
            try {
                int id = Integer.parseInt(reserva.getId());
                if (id > maiorId) {
                    maiorId = id;
                }
            } catch (NumberFormatException e) {
                // Ignora IDs não numéricos
            }
        }

        return String.valueOf(maiorId + 1);
    }

    /**
     * Busca reservas por período
     */
    public static List<Reserva> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        List<Reserva> reservas = carregarTodas();
        return reservas.stream()
                .filter(r -> r.getDataHoraInicio().isBefore(fim) && r.getDataHoraFim().isAfter(inicio))
                .collect(Collectors.toList());
    }

    /**
     * Busca reservas ativas
     */
    public static List<Reserva> buscarReservasAtivas() {
        List<Reserva> reservas = carregarTodas();
        return reservas.stream()
                .filter(r -> r.getStatus() == StatusReserva.CONFIRMADA || r.getStatus() == StatusReserva.PENDENTE)
                .collect(Collectors.toList());
    }

    /**
     * Verifica se existe reserva ativa para um espaço
     */
    public static boolean existeReservaAtivaParaEspaco(String espacoId) {
        List<Reserva> reservas = carregarTodas();
        return reservas.stream()
                .anyMatch(r -> r.getEspacoId().equals(espacoId) && (r.getStatus() == StatusReserva.CONFIRMADA || r.getStatus() == StatusReserva.PENDENTE));
    }
}


