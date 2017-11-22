package br.edu.ifpb.sockets.servidor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class Servidor extends Thread {

	private static ArrayList<BufferedWriter> clientes;
	private static ServerSocket server;
	private static ArrayList<String> clientesNomes;
	private String nome;
	private Socket con;
	private InputStream in;
	private InputStreamReader inr;
	private BufferedReader bfr;
	private String socketAddress;

	public Servidor(Socket con) {
		this.con = con;
		try {
			in = con.getInputStream();
			inr = new InputStreamReader(in);
			bfr = new BufferedReader(inr);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {

		try {

			String msg;
			OutputStream ou = this.con.getOutputStream();
			Writer ouw = new OutputStreamWriter(ou);
			BufferedWriter bfw = new BufferedWriter(ouw);
			clientes.add(bfw);
			nome = msg = bfr.readLine();
			clientesNomes.add(nome);
			
			msg = bfr.readLine();
			
			while (!"bye".equalsIgnoreCase(msg) && msg != null) {	
				
				// Contar a quantidade de comandos dentro da mensagem
				int counter = 0;
				for( int i=0; i<msg.length(); i++ ) {
				    if( msg.charAt(i) == ' ' ) {
				        counter++;
				    } 
				}
				
				// Tratar a mensagem recebida
				if (counter == 0) {
					if (msg.equals("list")) {
						list(bfw);
					}  else {
						invalidCommand(bfw, msg);
					}
				} else {
					String[] parts = msg.split(" ");
					
					if (parts[0].equals("send")) {
						if (parts[1].equals("-all")) {
							sendToAll(bfw, parts[2]);
						} else {
							invalidCommand(bfw, parts[1]);
						}
					} else {
						invalidCommand(bfw, parts[0]);
					}
				}
				
				msg = bfr.readLine();
			}
			
			//Desconectando usuário
			sendToAll(bfw, null);
			
			Thread.currentThread().interrupt();
			return;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void list(BufferedWriter bw) throws IOException{
		
		String result = "\n  Lista de usuários online: "  + "\r\n" ;
		
		for (String clienteNome : clientesNomes) {
			result += "   " + clienteNome + "\n";
		}
		
		bw.write(result);
		bw.flush();
	}
	
	public void invalidCommand(BufferedWriter bw, String msg) throws IOException{
		String result = "O comando \"" + msg + "\" é inválido." + "\n";
		result += "Abaixo a lista de comandos válidos:" + "\n";
		result += "  send -all <mensagem>                    Enviar mensagem ao grupo" + "\n";
		result += "  send -user <nome_usuario> <mensagem>    Enviar mensagem reservada" + "\n";
		result += "  list                                    Visualizar participantes" + "\n" ;
		result += "  rename <novo_nome>                      Renomear usuário" + "\n";
		result += "  bye                                     Sair do grupo" + "\n\n";
		bw.write(result);
		bw.flush();
	}
	
	public void sendToAll(BufferedWriter bwSaida, String msg) throws IOException {
		BufferedWriter bwS;

		for (Iterator<BufferedWriter> iterator = clientes.iterator(); iterator.hasNext();) {
			BufferedWriter bw = iterator.next();
			bwS = (BufferedWriter) bw;
			
			// Obtendo IP e Port
			socketAddress = this.con.getInetAddress().toString();
			socketAddress = socketAddress.substring(1);
			socketAddress = socketAddress + ":" + Integer.toString(this.con.getPort());
			
			// Obtendo data e hora
			String dateStamp = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
			String timeStamp = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
			
			if (msg == null) {
				bw.write("Usuario " + nome + " saiu da sala" + "\r\n" );
				//Remover bw
				if (bwSaida == bwS) {
					iterator.remove();
					removeName(nome);
				}
			} else {
				bw.write(socketAddress + "/~" + nome + " : " + msg + " " + timeStamp  + " " + dateStamp + "\r\n" );
			}
			
			bw.flush();
		}
	}
	
	public void removeName(String nome) throws IOException {
		for (Iterator<String> iterator = clientesNomes.iterator(); iterator.hasNext();) {
			String clienteNome = iterator.next();

			if (clienteNome == nome) {
				iterator.remove();
				break;
			}
			
		}
	}
	
	public static void main(String[] args) {

		try {
			// Cria os objetos necessário para instânciar o servidor
			JLabel lblMessage = new JLabel("Porta do Servidor:");
			JTextField txtPorta = new JTextField("12345");
			Object[] texts = { lblMessage, txtPorta };
			JOptionPane.showMessageDialog(null, texts);
			server = new ServerSocket(Integer.parseInt(txtPorta.getText()));
			clientes = new ArrayList<BufferedWriter>();
			clientesNomes = new ArrayList<String>();
			JOptionPane.showMessageDialog(null, "Servidor ativo na porta: " + txtPorta.getText());

			while (true) {
				System.out.println("BotChat: Aguardando conexão...");
				Socket con = server.accept();
				System.out.println("BotChat: Cliente conectado...");
				Thread t = new Servidor(con);
				t.start();
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}
