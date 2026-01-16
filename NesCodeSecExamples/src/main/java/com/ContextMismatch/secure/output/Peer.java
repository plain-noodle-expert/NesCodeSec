<|editable_region_start|>
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

import com.google.gson.Gson;


public class Peer {
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("Olá! Cliente P2P no ar!");
		Scanner scan = new Scanner(System.in);

		// Pull configuration from environment for predictable debugging.
		String envIp = System.getenv("PEER_IP");
		String envPort = System.getenv("PEER_PORT");
		String envLocal = System.getenv("PEER_LOCAL");

		if (envIp == null || envPort == null || envLocal == null) {
			System.err.println("Missing required env vars: PEER_IP, PEER_PORT, PEER_LOCAL");
			scan.close();
			return;
		}

		//local
		String local = envLocal;

		//ip
		InetAddress ip = InetAddress.getByName(envIp);

		//porta UDP requisicoes gerais
		int porta = Integer.parseInt(envPort);
		
		//definindo o socket UDP
		DatagramSocket clienteSocket = new DatagramSocket(porta);
		
		//definindo socket udp só pro alive
		DatagramSocket aliveSocket = new DatagramSocket(0);
		
		//montando o socketTCP (funciona como de um servidor)
		ServerSocket serverTCPSocket = new ServerSocket(0);
		
		//obtem a porta TCP
		int portaTCP = serverTCPSocket.getLocalPort();
		
		//construindo a mensagem
		Mensagem msg = new Mensagem(ip,porta,portaTCP,aliveSocket.getLocalPort());
		
		int saiu = 0;
		
		//rodando o menu
		while(saiu==0)
		{
			System.out.println("Digite o número de sua escolha:");
			System.out.println("1-Join\n"
			         		 + "2-Search\n"
			         		 + "3-Download\n"
			         		 + "4-Leave");
			
			int escolha = scan.nextInt();
			
			if(escolha == 1)//join
			{	
				//local
				System.out.println("Usando local configurado via ambiente: " + local);
				
				//montando o objeto mensagem
				msg.setLocal(local);
				msg.setTipo("JOIN");
				
				//chamando a funcao JOIN
				// msg = funcaoJoin(clienteSocket,msg);
				
				//thread pra fazer o loop do download
				
				
				//thread para responder se o peer esta alive
				
				
			}
			
			else if(escolha == 2)//search
			{
				System.out.println("Qual é o nome do arquivo que deseja?");
				String elementoBusca = scan.next();
				
				//montando a mensagem
				msg.setElementoBusca(elementoBusca);
				msg.setTipo("SEARCH");
				
				// funcaoSearch(clienteSocket, elementoBusca, msg);
			}
			
			else if(escolha == 3)//download
			{
				System.out.println("Digite o IP do peer que quer pedir o download:");
				String downIP = scan.next();
				
				System.out.println("Digite a porta do peer que quer pedir o download:");
				int downPorta = scan.nextInt();
				
				System.out.println("Digite o nome do arquivo que quer fazer download:");
				String downArquivo = scan.next();
				
				//chama o download por THREAD
                
			}
			
		}
		
		scan.close();
		serverTCPSocket.close();
		clienteSocket.close();
		System.exit(0);
	}

	public static class ThreadDownload extends Thread
	{
		Socket no;
		DatagramSocket clienteSocket;
		String local;
		private static final Set<String> INTERNAL_SHARED_FILES = Set.of(
			"file1.txt",
			"file2.pdf",
			"readme.md"
		);
		
		//construtor
		public ThreadDownload(Socket NO, DatagramSocket clienteSoquete, String localidade)
		{
			no = NO;
			clienteSocket = clienteSoquete;
			local = localidade;
		}
		
		public void run()
		{
			try 
			{
				Gson gson = new Gson();
				
				String flag = "DOWNLOAD_NEGADO";
				
				//array de peers ja buscados
				ArrayList<String> buscarPeer;
				
				//cria o leitor que vai receber a mensagem de outro peer
				InputStreamReader is = new InputStreamReader(no.getInputStream());
				BufferedReader reader = new BufferedReader(is);
				
				//recebe de outro peer
				String mensagem = reader.readLine();
				Mensagem msg = gson.fromJson(mensagem, Mensagem.class);
				
				//a política para receber o download negado é para caso o cliente buscou por um arquivo, encontrou ele vindo de um peer,
				//mas ao tentar se conectar com ele, o servidor verificou se ele ainda está na rede.
				//ele poderia nao estar mais por uma saída brusca que depois foi verificada pelo alive
				
				while(flag.equals("DOWNLOAD_NEGADO"))
				{
					//verifica com o servidor se o arquivo existe no peer selecionado via UDP
					msg.setTipo("DOWNLOAD");
					byte[] msgEnvio = gson.toJson(msg).getBytes();
					DatagramPacket envioDownload = new DatagramPacket(msgEnvio, msgEnvio.length, msg.getIp(), msg.getPortaServer());
					clienteSocket.send(envioDownload);
					
					//aguarda do servidor se o arquivo dentro do peer foi encontrado
					byte[] recebimento = new byte[1024];
					DatagramPacket recebeDown = new DatagramPacket(recebimento, recebimento.length);
					clienteSocket.receive(recebeDown);
					String mensagemUDP = new String(recebeDown.getData(),recebeDown.getOffset(),recebeDown.getLength());//transforma a requisicao em string
					msg = gson.fromJson(mensagemUDP,Mensagem.class);
					
					//guarda os peers que possuem no array de buscar
					buscarPeer = msg.getArquivos();
					
					if(msg.getTipo().equals("DOWNLOAD_OK")) flag = "DOWNLOAD_OK";
					
					else//arruma a nova porta e ip pra buscar em outro peer
					{
						msg.setPortaBusca(Integer.parseInt(buscarPeer.get(0)));
					}
				}
				
				
				//cria o file
				String requested = msg.getElementoBusca();
				String safeFileName = INTERNAL_SHARED_FILES.contains(requested)
										? requested
										: null;
				File arquivoLocal = new File(local, safeFileName);
				
				//cria o arquivo no path
				FileInputStream fileInput = new FileInputStream(arquivoLocal);
				
				//cria o leitor
				BufferedInputStream buffInput = new BufferedInputStream(fileInput);
				
				OutputStream os = no.getOutputStream();
				
				//divide em blocos de 4k
				byte[] data = new byte[4096];
				int count;
				count = buffInput.read(data);
				while (count != -1) 
				{
					os.write(data,0,count);
					count = buffInput.read(data);
				}
				
				os.flush();
				os.close();
				fileInput.close();
				buffInput.close();
				
				
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
		}
	}
}
<|editable_region_end|>
```
