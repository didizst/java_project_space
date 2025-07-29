package br.edu.espacos;

import br.edu.espacos.client.EspacosClient;
import br.edu.espacos.model.*;
import br.edu.espacos.storage.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Teste simples para verificar se as reservas estão sendo carregadas corretamente
 */
public class TestReservas {
    public static void main(String[] args) {

        
        try {
            // Criar dados de teste
            criarDadosTeste();
            
            // Testar cliente
            EspacosClient client = new EspacosClient();
            if (!client.conectar("localhost", 12346)) {

                return;
            }
            
            // Fazer login
            String loginResponse = client.login("admin@sistema.com", "admin123");

            
            if (loginResponse.startsWith("LOGIN_OK")) {
                String[] partes = loginResponse.split("\\|");
                String usuarioId = partes[3];

                
                // Listar espaços
                String espacosResponse = client.listarEspacos();

                
                // Fazer uma reserva de teste
                LocalDateTime agora = LocalDateTime.now().plusDays(1);
                String dataInicio = agora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String dataFim = agora.plusHours(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                
                String reservaResponse = client.fazerReserva("1", dataInicio, dataFim, "Teste de reserva", "Observações de teste");

                
                // Listar minhas reservas
                String minhasReservasResponse = client.listarMinhasReservas(usuarioId);

                
                // Parsear e exibir as reservas
                parseEExibirReservas(minhasReservasResponse);
            }
            
            client.desconectar();
            
        } catch (Exception e) {


        }
    }
    
    private static void criarDadosTeste() {

        
        // Criar usuário admin se não existir
        if (!UsuarioStorage.emailJaExiste("admin@sistema.com")) {
            Usuario admin = new Usuario("1", "Administrador", "admin@sistema.com", "admin123", TipoUsuario.ADMIN);
            UsuarioStorage.salvar(admin);

        }
        
        // Criar espaço de teste se não existir
        if (EspacoStorage.buscarPorId("1") == null) {
            SalaAula sala = new SalaAula("1", "Sala de Teste", "Prédio A", 30, "Sala para testes", false, false, true, 0);
            EspacoStorage.salvar(sala);

        }
    }
    
    private static void parseEExibirReservas(String resposta) {

        String[] partes = resposta.split("\\|");
        
        if (partes[0].equals("MINHAS_RESERVAS")) {
            if (partes.length > 1 && !partes[1].trim().isEmpty()) {
                String[] reservas = partes[1].split("@@");

                
                for (int i = 0; i < reservas.length; i++) {
                    String reserva = reservas[i];
                    if (!reserva.trim().isEmpty()) {
                        String[] dados = reserva.split(";");
                        if (dados.length >= 6) {

                            System.out.println("  ID: " + dados[0]);
                            System.out.println("  Espaço: " + dados[1]);
                            System.out.println("  Data Início: " + dados[2]);
                            System.out.println("  Data Fim: " + dados[3]);
                            System.out.println("  Finalidade: " + dados[4]);
                            System.out.println("  Status: " + dados[5]);
                        } else {
                            System.out.println("Reserva com dados incompletos: " + reserva);
                        }
                    }
                }
            } else {
                System.out.println("Nenhuma reserva encontrada.");
            }
        } else {
            System.out.println("Resposta inesperada: " + resposta);
        }
    }
}

