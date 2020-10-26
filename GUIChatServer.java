// Version 1.0.1
// GUI ȭ��
import java.awt.Button;
import java.awt.Frame;
import java.awt.TextArea;
// �̺�Ʈ ó��
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// �����
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
//improt java.neet.*; // TCP ���� 
// TCP ���� 
import java.net.ServerSocket;
import java.net.Socket;
// Vector Ŭ����
import java.util.Vector;

class GUIChatServer extends Frame implements ActionListener {
	Button btn_ext; // ���� ��ư
	TextArea txt_list; // ���� ��� ���
	protected Vector list; // ������ ���� ��� ����

	// ������
	public GUIChatServer(String title) {
		super(title); // Ÿ��Ʋ�ٿ� ��µ� ���ڿ�
		list = new Vector(); // ���� ����
		btn_ext = new Button("���� ����"); // ���� ���� ��ư ����
		btn_ext.addActionListener(this); // �̺�Ʈ ���
		txt_list = new TextArea(); // txt_list ����
		add("Center", txt_list); // ȭ�� ��� txt_list ���
		add("South", btn_ext); // ȭ�� �ϴܿ� ���� ���� ��ư ���
		setSize(400, 300); // ȭ�� ũ�� ����
		setVisible(true); // ȭ�� ���
		ServerStart(); // ä�� ���� ����
	}

	// ä�� ����
	public void ServerStart() {
		final int port = 5005; // ä�� ���� ��Ʈ ��� ����
		try {
			ServerSocket ss = new ServerSocket(port); // ServerSocket ����
			while (true) {
				Socket client = ss.accept(); // Ŭ���̾�Ʈ ���� ��ٸ�
				txt_list.appendText(client.getInetAddress().getHostAddress() + "\n");
				ChatHandle ch = new ChatHandle(this, client); // ChatHandle �ʱ�ȭ
				list.addElement(ch); // Ŭ���̾�Ʈ ���� list ���Ϳ� �߰�
				ch.start(); // ChatHandle ������ ����
			}
		} catch (Exception e) { // ���� ó��
			System.out.println(e.getMessage());
		}
	}

	// ���� ���� ��ư�� ������ ��
	public void actionPerformed(ActionEvent e) {
		System.exit(0);
	}

	// �޽��� ��� �޼���
	public void setMsg(String msg) {
		txt_list.appendText(msg + "\n");// ȭ�鿡 msg �޽��� ���
	}

	// main �Լ�
	public static void main(String[] args) {
		new GUIChatServer("ä�� ���� ");
	}
}

// ChatHandle Ŭ���� : ä�� ������ �������� ���� ó��

class ChatHandle extends Thread { // ������ ���
	GUIChatServer server = null; // GUIChatServer ��� ����
	Socket client = null; // ������ Ŭ���̾�Ʈ
	BufferedReader br = null; // �о����
	PrintWriter pw = null; // ������

	// ������
	public ChatHandle(GUIChatServer server, Socket client) throws IOException {
		this.server = server;
		this.client = client;
		// ����� ��Ʈ�� ����
		InputStream is = client.getInputStream();
		br = new BufferedReader(new InputStreamReader(is));
		OutputStream os = client.getOutputStream();
		pw = new PrintWriter(new OutputStreamWriter(os));
	}

	// ���� ������ ������ ��� Ŭ���̾�Ʈ�� msg����
	public void Send_All(String msg) {
		try {
			synchronized (server.list) { // GUIChatServer ��� ���� list ����ȭ ó��
				int size = server.list.size(); // ���� ������ Ŭ���̾�Ʈ ��
				for (int i = 0; i < size; i++) {
					ChatHandle chs = (ChatHandle) server.list.elementAt(i);
					synchronized (chs.pw) { // ChatHandle pw �ν��Ͻ��� �̿��� ���� ����
						chs.pw.println(msg);
					}
					chs.pw.flush();
				}
			}
		} catch (Exception e) { // ���� ó��
			System.out.println(e.getMessage());
		}
	}

	// Thread Ŭ������ run �޼��� �������̵�.
	public void run() {
		String name = "";
		try {
			name = br.readLine(); // ��ȭ�� �о����
			Send_All(name + " ���� ���� �����ϼ̽��ϴ�.");
			while (true) {
				String msg = br.readLine(); // Ŭ���̾�Ʈ �޽��� ���
				String str = client.getInetAddress().getHostName();
				synchronized (server) {
					server.setMsg(str + " : " + msg); // ���� Ŭ���̾�Ʈ �޽��� ���
				}
				if (msg.equals("@@Exit"))// @@Exit �޽����� Ŭ���̾�Ʈ ���� ����
					break;
				else // ���� ������ ��� Ŭ���̾�Ʈ�� �޽��� ����
					Send_All(name + " >> " + msg);
			}
		} catch (Exception e) { // ���� ó��
			server.setMsg(e.getMessage());
		} finally {
			synchronized (server.list) {
				server.list.removeElement(this); // ���� ��Ͽ��� ����
			}
			try { // ��Ʈ�� ����
				br.close();
				pw.close();
				client.close(); // Ŭ���̾�Ʈ ���� ����
			} catch (IOException e) { // ���� ó��
				server.setMsg(e.getMessage());
			}
		}
	}
}