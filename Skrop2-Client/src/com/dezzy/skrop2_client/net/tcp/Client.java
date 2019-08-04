package com.dezzy.skrop2_client.net.tcp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.dezzy.skrop2_client.game.ClientController;
import com.dezzy.skrop2_client.net.NetUtils;

public class Client implements Runnable {
	private static final String NEGOTIATION_KEY = "GaRZ}:zZO}o%L9<7&LWhNkSA@oPlMJ!&6QpC/+2Hgd_@{wp;0);R.)puQMJ~|:^fBYTs|SibxgR,5*TbPps1RAV)P'oG3XzaMO44`19\\8Rbhp>>M;p}5^qh>se#(TfV5rW7MOaP(;w?/v-DmR`N^rSl(-U)`:.~L%O0a\"DzJLUS`i&HscQ|vHwaZae/,#KG|\"i(z5@9ry=\"G`*l]Fy%^.9H=+.P&D=:j5BTW\"o~_XC(chSgUKh%8-ioyc\"A/~ns\\1*O6gC=irzguy<Ki#!Gq,f<<|V/Wf\\N2'dM0db/$2Kv>blGWf@-/I[kNu5GlD?$e'@EC=UZh{:.|JVt%v-[:9A>S4oqD{[xoI.a?tnHLy|XcVQJF[642SpOQoOKL;T2^YzT/H\\6N'XI]tq\"DgQGUaj0_m|wBFA1E&GCTG:i{9\"siXF\"]X99XJ3sV|xz^[yu>ALS|%,Ky!+_vlBcc[n[nlVDP+<4+9d,s\"Z:2jMvj0PZF&%pq3k)BCX9HU_bn-UWi1Fh0=T{lPz6TQ\"*/m:l=-w8Pt,g,Y#'r#ER;\"q*LJ<OWW$L6ti$]V*1z|q8YZpXBrZRK@MOX-#YvKC\\RiAA]D_[KLv;t${q'JDhk:C%G.1I>NQup>L#[&wwgq*j8M0S=S09,T{tWdyh%Wz{Vt}59bg5`4%ZvSJW]it@7'G8!2'I<O.c{D.I#R~eY%TvLHhU?Z.#O}A<PD;^SK[$`>\"vdgfSw9^60{J%`<~~=2i'+<8\"iV*/2#QqgbR6OWr~b?84i\\O(qs2KZZpJg%#!-JF+T|5W<`qu!6*j&Cs(+F9$<~E)0`:AuwK|M=zjn}Aw*6()dY$!5,:ddw9v+gP1yJ0EnDo%0tt>S?~Upw/`Nu5GRzIW%xwI6m\\3k::Df\"N4h2lF#F7I/C1c\"Dy,0#$apq.Okn7S0GajqZRu=!N2E%+@J|Wu/i4(?lSYYAbHR*',K-;rOecYli95%MMI>0=}(!~Sxmp1-#!X%O[#/O.ol7d@G%Whn%s0MZ#MyQv0jT8fRj\\h<eyQc\\7|5qHg+eTQ[&)MWqkaS81\"0w9@NqUZGnQse/[9cY_c2]4P($_5PKHN\\`$~\\0hl:M'\"PH<$xJ7zF!t&!p'ZI^x%gf.jh[Ri{FS{a2<Ba]%G=!=er4+!U*Z4na'n[y+']h!0O\"mFMzwvp-H%)!QnRl+e4RH_^/@(4RV{ZB|Lh0@%1?aB1[2{6+H,Aj-]ni_y(*5]'zL=/N1Nl>I~)ZaC8qK>o\"2aN6P>q'\"rq9.7$TQHPw5%5{TT:*Hs--EgAR}J;8&-O:Pc4v=^G46#~?oMy?(#Cx$^F(FH4Q[&Tus^0\\U!m2giSkAlwVri^D#-Z2YIuZ`|?2Q(}M@^!J6!e_qX73v'!L<1m[/jHM\"4h%(QT#S%Z2xYRaXkrV\"ZP@W5S1VFNtc:dlT56_l8AgcW<fOwTB/5h'FL}2oVbLJ<yYl*ErAjaI4FBr]5\"XxrB7-sg\"xU\\4mD.<mkgxtG*,|uA\\],_uEt&z2(@h8OwLU6LM<+|DAdq\\oQ\\2;dpD/uov9-EZxw<@xO%.X[oDjR,U@WYBZf:8B\"W+@la}.}){?{4h;K}/bFJ(\"f-Kq=8,8C8O1!&@B,ui5~&C<f:?-<RLWnybYvX,TDL4[<:GBDS}%_Oha0q-!<<F+c#R;4~`mGhHOg6}9_Atx3R2#\"KKot2:yN{AjEyY_l2RZa=xm\"$bQ]lr+~@+L0A%S$&CiO>/lrZh[~{x,8;/l6JBk0`.I^:l`*V*k<?6IjbLmiQ)-!9ct\"Qs^v_$W}N@yjNQ\\@7g5o8O7Bwf5-DqesW)4CU,deM17eVa3l_,}*p*E0u5Q[X+}twY\\GkG4!9-8uv1o^'UI[C^bJ).~aKhGAXO+l9s0iBGddzJ;:&sgzFa=Mzm8R`\"8rwhif&j_cHtP~c660MmhTQ17'-#JKdexm%zO'mk%lg'>!JNVGu6NbbJ<&J@XW$\"S;P/5\"#L;iMrQi:z~)=Zf~f`,m(L+IvV_qY^E|3z:/mV0nzGV9^zz{z?to*9%&'T$P0me&f=u(XbeU>#z$w~[>W~ozE";
	
	private volatile boolean newKeyNegotiated = false;
	private volatile String newKey;
	
	private final Socket socket;
	private final ClientController clientController;
	private volatile boolean isRunning = true;
	
	private final ConcurrentLinkedQueue<String> messageQueue;
	
	public Client(final ClientController _clientController, final String ip, int port) throws UnknownHostException, IOException {
		clientController = _clientController;
		
		messageQueue = new ConcurrentLinkedQueue<String>();
		
		socket = new Socket(ip, port);
		socket.setTcpNoDelay(true);
	}
	
	@Override
	public void run() {
		try {
			BufferedReader din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintStream dout = new PrintStream(new BufferedOutputStream(socket.getOutputStream()));
			String in = null;
			
			while (isRunning || !messageQueue.isEmpty()) {				
				if (din.ready()) {
					in = din.readLine();
					
					if (!newKeyNegotiated) {
						String keyInfo = NetUtils.decrypt(in, NEGOTIATION_KEY);
						
						if (keyInfo.startsWith("key ")) {
							newKey = keyInfo.substring(keyInfo.indexOf(" ") + 1);
						} else if (keyInfo.equals("quit") || keyInfo.equals("timeout")) {
							isRunning = false;
							messageQueue.clear();
							clientController.relayTCPMessage(keyInfo);
							break;
						}
					} else {
						String decrypted = NetUtils.decrypt(in, newKey);
						
						if (decrypted.equals("quit")) {
							isRunning = false;
							clientController.relayTCPMessage("quit");
							break;
						}
						
						clientController.relayTCPMessage(decrypted);
					}				
				} else {
					
					if (newKeyNegotiated) {
						String message = null;
						
						String out = "";					
						while ((message = messageQueue.poll()) != null) {
							out += message + "\r\n";
						}
						
						if (!out.isEmpty()) {
							dout.println(NetUtils.encrypt(out.trim(), newKey));
							dout.flush();
						}
					} else {
						if (newKey != null) {
							dout.println(NetUtils.encrypt("key-accepted", NEGOTIATION_KEY));
							dout.flush();
							newKeyNegotiated = true;
						}
					}
				}
			}
			messageQueue.clear();
			newKey = null;
			newKeyNegotiated = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		isRunning = false;
	}
	
	/**
	 * Tries to send a String to the server.
	 * 
	 * @param _message String to be sent
	 */
	public void sendString(final String _message) {
		messageQueue.add(_message);
	}
	
	public boolean sendingMessage() {
		return !messageQueue.isEmpty();
	}
	
	public void stop() {
		isRunning = false;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
}
