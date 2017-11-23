package br.edu.ifpb.sockets.cliente;

import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Cliente extends JFrame implements ActionListener, KeyListener {
	private static final long serialVersionUID = 1L;
	private JTextArea texto;
	private JTextField txtMsg;
	private JButton btnSend;
	private JButton btnSair;
	private JLabel lblHistorico;
	private JLabel lblMsg;
	private JPanel pnlContent;
	private Socket socket;
	private OutputStream ou;
	private Writer ouw;
	private BufferedWriter bfw;
	private JTextField txtIP;
	private JTextField txtPorta;
	private JTextField txtNome;
	private String socketAddress;

	public Cliente() throws IOException {
		JLabel lblMessage = new JLabel("Verificar!");
		txtIP = new JTextField("127.0.0.1");
		txtPorta = new JTextField("12345");
		txtNome = new JTextField("Cliente");
		Object[] texts = { lblMessage, txtIP, txtPorta, txtNome };
		JOptionPane.showMessageDialog(null, texts);
		pnlContent = new JPanel();
		texto = new JTextArea(12, 50);
		texto.setEditable(false);
		texto.setBackground(new Color(240, 240, 240));
		txtMsg = new JTextField(30);
		lblHistorico = new JLabel("Histórico");
		lblMsg = new JLabel("Mensagem");
		btnSend = new JButton("Enviar");
		btnSend.setToolTipText("Enviar Mensagem");
		btnSair = new JButton("Sair");
		btnSair.setToolTipText("Sair do Chat");
		btnSend.addActionListener(this);
		btnSair.addActionListener(this);
		btnSend.addKeyListener(this);
		txtMsg.addKeyListener(this);
		JScrollPane scroll = new JScrollPane(texto);
		texto.setLineWrap(true);
		pnlContent.add(lblHistorico);
		pnlContent.add(scroll);
		pnlContent.add(lblMsg);
		pnlContent.add(txtMsg);
		pnlContent.add(btnSair);
		pnlContent.add(btnSend);
		pnlContent.setBackground(Color.LIGHT_GRAY);
		texto.setBorder(BorderFactory.createEtchedBorder(Color.BLUE, Color.BLUE));
		txtMsg.setBorder(BorderFactory.createEtchedBorder(Color.BLUE, Color.BLUE));
		setTitle(txtNome.getText());
		setContentPane(pnlContent);
		setLocationRelativeTo(null);
		setResizable(false);
		setSize(600, 300);
		setVisible(true);
		//setDefaultCloseOperation(EXIT_ON_CLOSE);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
	}

	public void conectar() throws IOException {
		socket = new Socket(txtIP.getText(), Integer.parseInt(txtPorta.getText()));
		
		// Salvando o Address
		socketAddress = socket.getRemoteSocketAddress().toString();
		socketAddress = socketAddress.substring(1);
		
		ou = socket.getOutputStream();
		ouw = new OutputStreamWriter(ou);
		bfw = new BufferedWriter(ouw);
		bfw.write(txtNome.getText() + "\r\n");
		bfw.flush();	
	}

	public void enviarMensagem(String msg) throws IOException {

		if (msg.equals("bye")) {
			Window w = SwingUtilities.getWindowAncestor(pnlContent);
	        w.setVisible(false);
			bfw.close();
			ouw.close();
			ou.close();
		} else {
			bfw.write(msg + "\r\n");
			bfw.flush();
			txtMsg.setText("");
		}
	}

	public void escutar() throws IOException {

		InputStream in = socket.getInputStream();
		InputStreamReader inr = new InputStreamReader(in);
		BufferedReader bfr = new BufferedReader(inr);
		String msg = "";

		while (!"bye".equalsIgnoreCase(msg)) {
			if (bfr.ready()) {
				msg = bfr.readLine();
				if (msg.equals("bye")) {
					texto.append("Servidor caiu! \r\n");
				} else if (msg.toLowerCase().contains("username".toLowerCase())) {
					System.out.println("Teste mudar nome");
					String[] parts = msg.split(" ");
					txtNome = new JTextField(parts[1]);
				} else {
					texto.append(msg + "\r\n");
				}
			}
		}
	}

	public void sair() throws IOException {

		enviarMensagem("bye");
		bfw.close();
		ouw.close();
		ou.close();
		socket.close();
	}

	@Override
	public void keyPressed(KeyEvent e) {

		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			try {
					enviarMensagem(txtMsg.getText());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		try {
			if (e.getActionCommand().equals(btnSend.getActionCommand()))
				enviarMensagem(txtMsg.getText());
			else if (e.getActionCommand().equals(btnSair.getActionCommand()))
				sair();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) throws IOException {

		Cliente app = new Cliente();
		app.conectar();
		app.escutar();
	}
}
