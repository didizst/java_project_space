package br.edu.espacos.server;

import br.edu.espacos.model.*;
import br.edu.espacos.storage.*;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ClientHandlerEspacos implements Runnable {
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private Socket clientSocket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String usuarioLogado = null;

    public ClientHandlerEspacos(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            // Envia mensagem de boas-vindas
            enviarMensagem("CONECTADO|Bem-vindo ao Sistema de Gestão de Espaços Acadêmicos");

            String linha;
            while ((linha = reader.readLine()) != null) {

                processarComando(linha);
            }

        } catch (IOException e) {

        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {

            }
        }
    }

    private void processarComando(String comando) throws IOException {
        String[] partes = comando.split("\\|");
        String acao = partes[0];

        try {
            switch (acao) {
                case "LOGIN":
                    processarLogin(partes);
                    break;
                case "REGISTRAR":
                    processarRegistro(partes);
                    break;
                case "LISTAR_ESPACOS":
                    listarEspacos();
                    break;
                case "CADASTRAR_ESPACO":
                    cadastrarEspaco(partes);
                    break;
                case "REMOVER_ESPACO":
                    removerEspaco(partes);
                    break;
                case "BUSCAR_DETALHES_ESPACO":
                    buscarDetalhesEspaco(partes);
                    break;
                case "LISTAR_RESERVAS":
                    listarReservas();
                    break;
                case "LISTAR_MINHAS_RESERVAS":
                    listarMinhasReservas(partes[1]);
                    break;
                case "FAZER_RESERVA":
                    fazerReserva(partes);
                    break;
                case "CANCELAR_RESERVA":
                    cancelarReserva(partes);
                    break;
                case "VERIFICAR_DISPONIBILIDADE":
                    verificarDisponibilidade(partes);
                    break;
                case "GERAR_RELATORIO":
                    gerarRelatorio();
                    break;
                case "LOGOUT":
                    usuarioLogado = null;
                    enviarMensagem("LOGOUT_OK|Logout realizado com sucesso");
                    break;
                case "EXPORTAR_TODAS_RESERVAS":
                    exportarTodasReservas();
                    break;
                case "LISTAR_USUARIOS":
                    listarUsuarios();
                    break;

                case "DELETAR_USUARIO":
                    deletarUsuario(partes);
                    break;

                case "ALTERAR_TIPO_USUARIO":
                    alterarTipoUsuario(partes);
                    break;
                default:
                    enviarMensagem("ERRO|Comando não reconhecido: " + acao);
            }
        } catch (Exception e) {


            enviarMensagem("ERRO|Erro interno do servidor: " + e.getMessage());
        }
    }

    private void processarLogin(String[] partes) throws IOException {
        if (partes.length < 3) {
            enviarMensagem("ERRO|Formato inválido para login");
            return;
        }

        String email = partes[1];
        String senha = partes[2];

        Usuario usuario = UsuarioStorage.buscarPorEmail(email);
        if (usuario != null && usuario.getSenha().equals(senha) && usuario.isAtivo()) {
            usuarioLogado = usuario.getId();
            enviarMensagem("LOGIN_OK|" + usuario.getNome() + "|" + usuario.getTipo().name() + "|" + usuario.getId());

        } else {
            enviarMensagem("LOGIN_ERRO|Email ou senha incorretos");

        }
    }

    private void processarRegistro(String[] partes) throws IOException {
        if (partes.length < 5) {
            enviarMensagem("ERRO|Formato inválido para registro");
            return;
        }

        String nome = partes[1];
        String email = partes[2];
        String senha = partes[3];
        String tipoStr = partes[4];

        if (UsuarioStorage.emailJaExiste(email)) {
            enviarMensagem("REGISTRO_ERRO|Email já existe");
            return;
        }

        try {
            TipoUsuario tipo = TipoUsuario.valueOf(tipoStr);
            String novoId = UsuarioStorage.gerarNovoId();
            Usuario novoUsuario = new Usuario(novoId, nome, email, senha, tipo);
            UsuarioStorage.salvar(novoUsuario);
            enviarMensagem("REGISTRO_OK|Usuário registrado com sucesso");

        } catch (IllegalArgumentException e) {
            enviarMensagem("REGISTRO_ERRO|Tipo de usuário inválido");
        }
    }

    private void exportarTodasReservas() throws IOException {
        if (usuarioLogado == null) {
            enviarMensagem("ERRO|Usuário não logado");
            return;
        }

        Usuario usuario = UsuarioStorage.buscarPorId(usuarioLogado);
        if (usuario == null || usuario.getTipo() != TipoUsuario.ADMIN) {
            enviarMensagem("ERRO|Apenas administradores podem exportar reservas");
            return;
        }

        List<Reserva> todasReservas = ReservaStorage.carregarTodas();
        StringBuilder sb = new StringBuilder("TODAS_RESERVAS");

        if (!todasReservas.isEmpty()) {
            sb.append("|");
            for (int i = 0; i < todasReservas.size(); i++) {
                Reserva reserva = todasReservas.get(i);
                Espaco espaco = EspacoStorage.buscarPorId(reserva.getEspacoId());
                Usuario reservaUsuario = UsuarioStorage.buscarPorId(reserva.getUsuarioId());

                String espacoNome = (espaco != null) ? espaco.getNome() : "Espaço Desconhecido";
                String usuarioNome = (reservaUsuario != null) ? reservaUsuario.getNome() : "Usuário Desconhecido";

                sb.append(reserva.getId()).append("###")
                        .append(espacoNome).append("###")
                        .append(usuarioNome).append("###")
                        .append(reserva.getDataHoraInicio().format(INPUT_FORMATTER)).append("###")
                        .append(reserva.getDataHoraFim().format(INPUT_FORMATTER)).append("###")
                        .append(reserva.getFinalidade()).append("###")
                        .append(reserva.getObservacoes()).append("###")
                        .append(reserva.getStatus().name());

                if (i < todasReservas.size() - 1) {
                    sb.append("@@@");
                }
            }
        } else {
            sb.append("|");
        }

        enviarMensagem(sb.toString());
    }


    private void listarEspacos() throws IOException {
        if (usuarioLogado == null) {
            enviarMensagem("ERRO|Usuário não logado");
            return;
        }

        List<Espaco> espacos = EspacoStorage.carregarTodos();
        StringBuilder sb = new StringBuilder("ESPACOS");

        if (!espacos.isEmpty()) {
            sb.append("|");
            boolean primeiro = true;
            for (Espaco espaco : espacos) {
                if (espaco.isAtivo()) {
                    if (!primeiro) {
                        sb.append(":");
                    }
                    sb.append(espaco.getId()).append(";")
                            .append(espaco.getNome()).append(";")
                            .append(espaco.getTipo().name()).append(";")
                            .append(espaco.getLocalizacao()).append(";")
                            .append(espaco.getCapacidade());
                    primeiro = false;
                }
            }
        } else {
            sb.append("|");
        }

        enviarMensagem(sb.toString());
    }

    private void cadastrarEspaco(String[] partes) throws IOException {
        if (usuarioLogado == null) {
            enviarMensagem("ERRO|Usuário não logado");
            return;
        }

        // Verificar se é admin
        Usuario usuario = UsuarioStorage.buscarPorId(usuarioLogado);
        if (usuario == null || usuario.getTipo() != TipoUsuario.ADMIN) {
            enviarMensagem("ERRO|Apenas administradores podem cadastrar espaços");
            return;
        }

        if (partes.length < 6) { // Nome, Localizacao, Capacidade, Descricao, Tipo, AtributosEspecificos
            enviarMensagem("CADASTRO_ESPACO_ERRO|Formato inválido para cadastro de espaço");
            return;
        }

        try {
            String novoId = EspacoStorage.gerarNovoId(); // Gerar ID automaticamente
            String nome = partes[1];
            String localizacao = partes[2];
            int capacidade = Integer.parseInt(partes[3]);
            String descricao = partes[4];
            TipoEspaco tipo = TipoEspaco.valueOf(partes[5]);
            String atributosEspecificosStr = partes.length > 6 ? partes[6] : "";

            if (EspacoStorage.nomeJaExiste(nome)) {
                enviarMensagem("CADASTRO_ESPACO_ERRO|Nome já existe");
                return;
            }

            Espaco novoEspaco;
            String[] atributos = atributosEspecificosStr.split(";");

            // Criar o tipo específico de espaço baseado no tipo e nos atributos
            switch (tipo) {
                case SALA_AULA:
                    novoEspaco = new SalaAula(novoId, nome, localizacao, capacidade, descricao,
                            Boolean.parseBoolean(atributos[0]),
                            Boolean.parseBoolean(atributos[1]),
                            Boolean.parseBoolean(atributos[2]),
                            Integer.parseInt(atributos[3]));
                    break;
                case LABORATORIO:
                    novoEspaco = new Laboratorio(novoId, nome, localizacao, capacidade, descricao,
                            Integer.parseInt(atributos[0]),
                            atributos[1],
                            Boolean.parseBoolean(atributos[2]),
                            Boolean.parseBoolean(atributos[3]),
                            atributos[4]);
                    break;
                case SALA_REUNIAO:
                    novoEspaco = new SalaReuniao(novoId, nome, localizacao, capacidade, descricao,
                            Boolean.parseBoolean(atributos[0]),
                            Boolean.parseBoolean(atributos[1]),
                            Boolean.parseBoolean(atributos[2]),
                            Boolean.parseBoolean(atributos[3]),
                            atributos[4]);
                    break;
                case QUADRA_ESPORTIVA:
                    novoEspaco = new QuadraEsportiva(novoId, nome, localizacao, capacidade, descricao,
                            atributos[0],
                            Boolean.parseBoolean(atributos[1]),
                            Boolean.parseBoolean(atributos[2]),
                            Boolean.parseBoolean(atributos[3]),
                            atributos[4]);
                    break;
                case AUDITORIO:
                    novoEspaco = new Auditorio(novoId, nome, localizacao, capacidade, descricao,
                            Boolean.parseBoolean(atributos[0]),
                            Boolean.parseBoolean(atributos[1]),
                            Boolean.parseBoolean(atributos[2]),
                            Boolean.parseBoolean(atributos[3]),
                            Integer.parseInt(atributos[4]),
                            atributos[5]);
                    break;
                default:
                    enviarMensagem("CADASTRO_ESPACO_ERRO|Tipo de espaço não suportado");
                    return;
            }

            EspacoStorage.salvar(novoEspaco);
            enviarMensagem("SUCESSO|Espaço cadastrado com sucesso");


        } catch (NumberFormatException e) {
            enviarMensagem("CADASTRO_ESPACO_ERRO|Erro de formato numérico nos atributos: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            enviarMensagem("CADASTRO_ESPACO_ERRO|Tipo de espaço inválido ou atributos incompletos: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            enviarMensagem("CADASTRO_ESPACO_ERRO|Atributos específicos insuficientes para o tipo de espaço selecionado.");
        }
    }

    private void removerEspaco(String[] partes) throws IOException {
        if (usuarioLogado == null) {
            enviarMensagem("ERRO|Usuário não logado");
            return;
        }

        // Verificar se é admin
        Usuario usuario = UsuarioStorage.buscarPorId(usuarioLogado);
        if (usuario == null || usuario.getTipo() != TipoUsuario.ADMIN) {
            enviarMensagem("ERRO|Apenas administradores podem remover espaços");
            return;
        }

        if (partes.length < 2) {
            enviarMensagem("REMOVER_ESPACO_ERRO|ID do espaço não informado");
            return;
        }

        String espacoId = partes[1];
        Espaco espaco = EspacoStorage.buscarPorId(espacoId);

        if (espaco == null) {
            enviarMensagem("REMOVER_ESPACO_ERRO|Espaço não encontrado");
            return;
        }

        // Verificar se há reservas ativas para este espaço
        if (ReservaStorage.existeReservaAtivaParaEspaco(espacoId)) {
            enviarMensagem("REMOVER_ESPACO_ERRO|Não é possível remover espaço com reservas ativas.");
            return;
        }

        EspacoStorage.remover(espacoId);
        enviarMensagem("REMOVER_ESPACO_OK|Espaço removido com sucesso");

    }

    private void buscarDetalhesEspaco(String[] partes) throws IOException {
        if (usuarioLogado == null) {
            enviarMensagem("ERRO|Usuário não logado");
            return;
        }

        if (partes.length < 3) {
            enviarMensagem("ERRO|Formato inválido para buscar detalhes do espaço");
            return;
        }

        String espacoId = partes[1];
        String tipoEspacoStr = partes[2];

        Espaco espaco = EspacoStorage.buscarPorId(espacoId);

        if (espaco == null) {
            enviarMensagem("ERRO|Espaço não encontrado");
            return;
        }

        // Verifica se o tipo do espaço corresponde ao esperado
        if (!espaco.getTipo().name().equals(tipoEspacoStr)) {
            enviarMensagem("ERRO|Tipo de espaço não corresponde ao ID fornecido");
            return;
        }

        StringBuilder sb = new StringBuilder("DETALHES_ESPACO|");
        Map<String, String> atributos = espaco.getAtributosEspecificosMap();
        boolean primeiro = true;
        for (Map.Entry<String, String> entry : atributos.entrySet()) {
            if (!primeiro) {
                sb.append(";");
            }
            sb.append(entry.getKey()).append(": ").append(entry.getValue());
            primeiro = false;
        }
        enviarMensagem(sb.toString());
    }

    private void fazerReserva(String[] partes) throws IOException {
        if (usuarioLogado == null) {
            enviarMensagem("ERRO|Usuário não logado");
            return;
        }

        if (partes.length < 6) {
            enviarMensagem("RESERVA_ERRO|Formato inválido para reserva");
            return;
        }

        try {
            String espacoId = partes[1];
            String dataHoraInicioStr = partes[2];
            String dataHoraFimStr = partes[3];
            String finalidade = partes[4];
            String observacoes = partes[5];

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dataHoraInicio = LocalDateTime.parse(dataHoraInicioStr, formatter);
            LocalDateTime dataHoraFim = LocalDateTime.parse(dataHoraFimStr, formatter);

            // Verificar se o espaço existe
            Espaco espaco = EspacoStorage.buscarPorId(espacoId);
            if (espaco == null) {
                enviarMensagem("RESERVA_ERRO|Espaço não encontrado");
                return;
            }

            // Obter o usuário logado para verificar o tipo
            Usuario usuario = UsuarioStorage.buscarPorId(usuarioLogado);
            if (usuario == null) {
                enviarMensagem("ERRO|Usuário logado não encontrado.");
                return;
            }

            // Criar nova reserva
            String novoId = ReservaStorage.gerarNovoId();
            Reserva novaReserva;
            if (usuario.getTipo() == TipoUsuario.ADMIN) {
                novaReserva = new Reserva(novoId, espacoId, usuarioLogado, dataHoraInicio, dataHoraFim, finalidade, observacoes, StatusReserva.CONFIRMADA);
            } else {
                novaReserva = new Reserva(novoId, espacoId, usuarioLogado, dataHoraInicio, dataHoraFim, finalidade, observacoes);
            }

            // Verificar conflitos
            if (ReservaStorage.temConflito(novaReserva)) {
                enviarMensagem("RESERVA_ERRO|Horário não disponível");
                return;
            }
            ReservaStorage.salvar(novaReserva);
            enviarMensagem("SUCESSO|Reserva realizada com sucesso! ID: " + novaReserva.getId());
        } catch (Exception e) {
            enviarMensagem("RESERVA_ERRO|Erro ao processar reserva: " + e.getMessage());
        }
    }

    private void cancelarReserva(String[] partes) throws IOException {
        if (usuarioLogado == null) {
            enviarMensagem("ERRO|Usuário não logado");
            return;
        }

        if (partes.length < 2) {
            enviarMensagem("CANCELAR_ERRO|ID da reserva não informado");

            return;
        }

        String reservaId = partes[1];


        Reserva reserva = ReservaStorage.buscarPorId(reservaId);


        if (reserva == null) {
            enviarMensagem("CANCELAR_ERRO|Reserva não encontrada");

            return;
        }

        if (!reserva.getUsuarioId().equals(usuarioLogado)) {
            enviarMensagem("CANCELAR_ERRO|Você só pode cancelar suas próprias reservas");

            return;
        }

        ReservaStorage.cancelar(reservaId);
        enviarMensagem("CANCELAR_OK|Reserva cancelada com sucesso");

    }

    private void verificarDisponibilidade(String[] partes) throws IOException {
        if (usuarioLogado == null) {
            enviarMensagem("ERRO|Usuário não logado");
            return;
        }

        if (partes.length < 4) {
            enviarMensagem("ERRO|Formato inválido para verificação de disponibilidade");
            return;
        }

        try {
            String espacoId = partes[1];
            String dataHoraInicioStr = partes[2];
            String dataHoraFimStr = partes[3];
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dataHoraInicio = LocalDateTime.parse(dataHoraInicioStr, formatter);
            LocalDateTime dataHoraFim = LocalDateTime.parse(dataHoraFimStr, formatter);

            List<Reserva> reservasConflitantes = ReservaStorage.buscarReservasAtivasPorEspacoEPeriodo(
                    espacoId, dataHoraInicio, dataHoraFim);

            if (reservasConflitantes.isEmpty()) {
                enviarMensagem("DISPONIBILIDADE|DISPONIVEL");
            } else {
                enviarMensagem("DISPONIBILIDADE|OCUPADO");
            }

        } catch (Exception e) {
            enviarMensagem("ERRO|Erro ao verificar disponibilidade: " + e.getMessage());
        }
    }

    private void gerarRelatorio() throws IOException {
        if (usuarioLogado == null) {
            enviarMensagem("ERRO|Usuário não logado");
            return;
        }

        // Verificar se é admin
        Usuario usuario = UsuarioStorage.buscarPorId(usuarioLogado);
        if (usuario == null || usuario.getTipo() != TipoUsuario.ADMIN) {
            enviarMensagem("ERRO|Apenas administradores podem gerar relatórios");
            return;
        }

        StringBuilder relatorio = new StringBuilder();
        relatorio.append("RELATORIO|");

        // Total de espaços cadastrados
        List<Espaco> todosEspacos = EspacoStorage.carregarTodos();
        relatorio.append("Total de Espaços Cadastrados: ").append(todosEspacos.size()).append("|");

        // Espaços ativos vs inativos
        long espacosAtivos = todosEspacos.stream().filter(Espaco::isAtivo).count();
        relatorio.append("Espaços Ativos: ").append(espacosAtivos).append("|");
        relatorio.append("Espaços Inativos: ").append(todosEspacos.size() - espacosAtivos).append("|");

        // Total de usuários cadastrados
        List<Usuario> todosUsuarios = UsuarioStorage.carregarTodos();
        relatorio.append("Total de Usuários Cadastrados: ").append(todosUsuarios.size()).append("|");

        // Usuários por tipo
        relatorio.append("Usuários Administradores: ").append(todosUsuarios.stream().filter(u -> u.getTipo() == TipoUsuario.ADMIN).count()).append("|");
        relatorio.append("Usuários Comuns: ").append(todosUsuarios.stream().filter(u -> u.getTipo() == TipoUsuario.COMUM).count()).append("|");

        // Total de reservas
        List<Reserva> todasReservas = ReservaStorage.carregarTodas();
        relatorio.append("Total de Reservas: ").append(todasReservas.size()).append("|");

        // Reservas por status
        relatorio.append("Reservas Pendentes: ").append(todasReservas.stream().filter(r -> r.getStatus() == StatusReserva.PENDENTE).count()).append("|");
        relatorio.append("Reservas Confirmadas: ").append(todasReservas.stream().filter(r -> r.getStatus() == StatusReserva.CONFIRMADA).count()).append("|");
        relatorio.append("Reservas Canceladas: ").append(todasReservas.stream().filter(r -> r.getStatus() == StatusReserva.CANCELADA).count()).append("|");
        relatorio.append("Reservas Concluídas: ").append(todasReservas.stream().filter(r -> r.getStatus() == StatusReserva.CONCLUIDA).count()).append("|");

        enviarMensagem(relatorio.toString());
    }

    private void listarReservas() throws IOException {
        if (usuarioLogado == null) {
            enviarMensagem("ERRO|Usuário não logado");
            return;
        }

        Usuario usuario = UsuarioStorage.buscarPorId(usuarioLogado);
        if (usuario == null || usuario.getTipo() != TipoUsuario.ADMIN) {
            enviarMensagem("ERRO|Apenas administradores podem listar todas as reservas");
            return;
        }

        List<Reserva> reservas = ReservaStorage.carregarTodas();
        StringBuilder sb = new StringBuilder("RESERVAS");

        if (!reservas.isEmpty()) {
            sb.append("|");
            boolean primeiro = true;
            for (Reserva reserva : reservas) {
                if (!primeiro) {
                    sb.append(":");
                }
                Espaco espaco = EspacoStorage.buscarPorId(reserva.getEspacoId());
                String nomeEspaco = (espaco != null) ? espaco.getNome() : "Desconhecido";
                String tipoEspaco = (espaco != null) ? espaco.getTipo().name() : "Desconhecido";
                Usuario userReserva = UsuarioStorage.buscarPorId(reserva.getUsuarioId());
                String nomeUsuarioReserva = (userReserva != null) ? userReserva.getNome() : "Desconhecido";

                sb.append(reserva.getId()).append(";")
                        .append(nomeEspaco).append(";")
                        .append(tipoEspaco).append(";")
                        .append(nomeUsuarioReserva).append(";")
                        .append(reserva.getDataHoraInicio().format(INPUT_FORMATTER)).append(";")
                        .append(reserva.getDataHoraFim().format(INPUT_FORMATTER)).append(";")
                        .append(reserva.getStatus().name()).append(";")
                        .append(reserva.getFinalidade());
                primeiro = false;
            }
        } else {
            sb.append("|");
        }
        enviarMensagem(sb.toString());
    }

    private void listarMinhasReservas(String usuarioId) throws IOException {
        if (usuarioLogado == null || !usuarioLogado.equals(usuarioId)) {
            enviarMensagem("ERRO|Acesso negado ou usuário não logado");
            return;
        }

        List<Reserva> reservas = ReservaStorage.buscarReservasPorUsuario(usuarioId);
        StringBuilder sb = new StringBuilder("MINHAS_RESERVAS");

        if (!reservas.isEmpty()) {
            sb.append("|");
            boolean primeiro = true;
            for (Reserva reserva : reservas) {
                if (!primeiro) {
                    sb.append("###");
                }
                Espaco espaco = EspacoStorage.buscarPorId(reserva.getEspacoId());
                String nomeEspaco = (espaco != null) ? espaco.getNome() : "Desconhecido";
                String tipoEspaco = (espaco != null) ? espaco.getTipo().name() : "Desconhecido";

                sb.append(reserva.getId()).append(";")
                        .append(nomeEspaco).append(";")
                        .append(tipoEspaco).append(";")
                        .append(reserva.getDataHoraInicio().format(INPUT_FORMATTER)).append(";")
                        .append(reserva.getDataHoraFim().format(INPUT_FORMATTER)).append(";")
                        .append(reserva.getStatus().name()).append(";")
                        .append(reserva.getFinalidade());
                primeiro = false;
            }
        } else {
            sb.append("|");
        }
        enviarMensagem(sb.toString());
    }

    private void listarUsuarios() throws IOException {
        if (usuarioLogado == null) {
            enviarMensagem("ERRO|Usuário não logado");
            return;
        }

        // Verificar se é admin
        Usuario usuario = UsuarioStorage.buscarPorId(usuarioLogado);
        if (usuario == null || usuario.getTipo() != TipoUsuario.ADMIN) {
            enviarMensagem("ERRO|Apenas administradores podem listar usuários");
            return;
        }

        List<Usuario> usuarios = UsuarioStorage.carregarTodos();
        StringBuilder sb = new StringBuilder("USUARIOS");

        if (!usuarios.isEmpty()) {
            sb.append("|");
            for (int i = 0; i < usuarios.size(); i++) {
                Usuario u = usuarios.get(i);
                sb.append(u.getId()).append(";")
                        .append(u.getNome()).append(";")
                        .append(u.getEmail()).append(";")
                        .append(u.getTipo().name()).append(";")
                        .append(u.isAtivo());
                if (i < usuarios.size() - 1) {
                    sb.append("@@@");
                }
            }
        } else {
            sb.append("|");
        }

        enviarMensagem(sb.toString());
    }

    private void deletarUsuario(String[] partes) throws IOException {
        if (usuarioLogado == null) {
            enviarMensagem("ERRO|Usuário não logado");
            return;
        }

        // Verificar se é admin
        Usuario usuario = UsuarioStorage.buscarPorId(usuarioLogado);
        if (usuario == null || usuario.getTipo() != TipoUsuario.ADMIN) {
            enviarMensagem("ERRO|Apenas administradores podem deletar usuários");
            return;
        }

        if (partes.length < 2) {
            enviarMensagem("ERRO|ID do usuário não informado");
            return;
        }

        String usuarioId = partes[1];
        if (usuarioId.equals(usuarioLogado)) {
            enviarMensagem("ERRO|Você não pode deletar a si mesmo");
            return;
        }

        Usuario usuarioADeletar = UsuarioStorage.buscarPorId(usuarioId);
        if (usuarioADeletar == null) {
            enviarMensagem("ERRO|Usuário não encontrado");
            return;
        }

        // Marcar como inativo
        usuarioADeletar.setAtivo(false);
        UsuarioStorage.atualizar(usuarioADeletar);
        enviarMensagem("SUCESSO|Usuário deletado com sucesso");
    }

    private void alterarTipoUsuario(String[] partes) throws IOException {
        if (usuarioLogado == null) {
            enviarMensagem("ERRO|Usuário não logado");
            return;
        }

        // Verificar se é admin
        Usuario usuario = UsuarioStorage.buscarPorId(usuarioLogado);
        if (usuario == null || usuario.getTipo() != TipoUsuario.ADMIN) {
            enviarMensagem("ERRO|Apenas administradores podem alterar tipos de usuário");
            return;
        }

        if (partes.length < 3) {
            enviarMensagem("ERRO|Parâmetros insuficientes");
            return;
        }

        String usuarioId = partes[1];
        String novoTipoStr = partes[2];

        if (usuarioId.equals(usuarioLogado)) {
            enviarMensagem("ERRO|Você não pode alterar seu próprio tipo");
            return;
        }

        TipoUsuario novoTipo;
        try {
            novoTipo = TipoUsuario.valueOf(novoTipoStr);
        } catch (IllegalArgumentException e) {
            enviarMensagem("ERRO|Tipo de usuário inválido");
            return;
        }

        Usuario usuarioAlvo = UsuarioStorage.buscarPorId(usuarioId);
        if (usuarioAlvo == null) {
            enviarMensagem("ERRO|Usuário não encontrado");
            return;
        }

        usuarioAlvo.setTipo(novoTipo);
        UsuarioStorage.atualizar(usuarioAlvo);
        enviarMensagem("SUCESSO|Tipo de usuário alterado com sucesso");
    }

    private void enviarMensagem(String mensagem) throws IOException {
        writer.write(mensagem);
        writer.newLine();
        writer.flush();
    }

    private void salvarReservaEmArquivo(Reserva reserva) {
        String fileName = "reservas_log.txt";
        try (FileWriter fw = new FileWriter(fileName, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(reserva.toFileString());
        } catch (IOException e) {
            System.err.println("Erro ao salvar reserva em arquivo: " + e.getMessage());
        }
    }
}


