/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.ipc.sink.chronicle.queue;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.ipc.sink.chronicle.queue.ChronicleQueueWrapper;
import org.opennms.core.ipc.sink.chronicle.queue.QueueDispatcher;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.XmlHandler;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
import org.opennms.netmgt.trapd.TrapDTO;
import org.opennms.netmgt.trapd.TrapLogDTO;
import org.snmp4j.PDU;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.VariableBinding;

import net.openhft.chronicle.bytes.Bytes;


public class ChronicleQueueWrapperTest {

    @Before
    public void setup() {
        System.setProperty("org.opennms.core.chronicle.queue.filedir", "/home/chandra/dev/test");
    }

    @Test
    public void testChronicleQueue() throws InterruptedException, UnknownHostException {

        ChronicleQueueWrapper adapter = new ChronicleQueueWrapper();
        for (int i = 1; i <= 40000; i++) {
            //String message = "This is chronicle queue writing " + i + " message";
            String message = generateTrapMessage(i);
            adapter.writeBytesForModule("Traps", message.getBytes(StandardCharsets.UTF_8));
        }
      
        AtomicInteger count = new AtomicInteger();
        AtomicInteger count1 = new AtomicInteger();
        Bytes.elasticByteBuffer();
        
        QueueDispatcher dispatcher = new QueueDispatcher() {

            @Override
            public void dispatch(byte[] bytes) {
                String matcher = new String(bytes, StandardCharsets.UTF_8);
                int num = count.incrementAndGet();
                XmlHandler<TrapLogDTO> xmlHandler = new XmlHandler<>(TrapLogDTO.class);
                TrapLogDTO trapLog = null;
                if ( num == 38) {
                    System.out.println("debug here");
                }
                try {
                  trapLog = xmlHandler.unmarshal(matcher);
                  //System.out.println(trapLog.getSystemId());
                  assertEquals(Integer.toString(num), trapLog.getSystemId());
                  count1.incrementAndGet();
                }catch (Exception e) {
                    //pass
                    System.out.println("skipped " + num);
                }
                
                //String message = "This is chronicle queue writing "  + num + " message";
                //System.out.println(matcher);
                //assertEquals(message, matcher);
            }
        };

        adapter.createTailerForModule("Traps", dispatcher);
        //adapter.stop();
        await().atMost(5, TimeUnit.MINUTES).untilAtomic(count, equalTo(40000));
        System.out.println(count1);
        adapter.stop();
    }

     private String generateTrapMessage(int trapNumber) throws UnknownHostException {
         PDU snmp4JV2cTrapPdu = new PDU();
         snmp4JV2cTrapPdu.setType(PDU.TRAP);
         OID oid = new OID(".1.3.6.1.2.1.1.3.0");
         snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));
         snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, oid));
         snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,new IpAddress("127.0.0.1")));

         snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString("Trap Msg v2-1")));
         snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString("Trap Msg v2-2")));

         snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"),
                 new OctetString("Trap v1 msg-1")));
         snmp4JV2cTrapPdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.3"),
                 new OctetString("Trap v1 msg-2")));
         snmp4JV2cTrapPdu.add(new VariableBinding(new OID(".1.3.6.1.6.3.1.1.4.1.1"), 
                 new OctetString("Trap v1 msg-3")));
         snmp4JV2cTrapPdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.733.6.3.18.1.5.0"),
                 new Integer32(1))); 
         snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"),
                 new Null())); 
         snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.1"),
                 new Null(128)));
         snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.2"),
                 new Null(129)));
         snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.3"),
                 new Null(130)));

         TrapInformation snmp4JV2cTrap = new Snmp4JTrapNotifier.Snmp4JV2TrapInformation(
             InetAddressUtils.ONE_TWENTY_SEVEN,
             "public",
             snmp4JV2cTrapPdu
         );

         TrapDTO trapDto = new TrapDTO(snmp4JV2cTrap);
         List<TrapDTO> trapMessages = new ArrayList<>();
         trapMessages.add(trapDto);
         
         TrapLogDTO trapLog = new TrapLogDTO();
         trapLog.setSystemId(Integer.toString(trapNumber));
         trapLog.setLocation("Default");
         trapLog.setTrapAddress(InetAddress.getLocalHost());
         trapLog.setMessages(trapMessages);
         XmlHandler<TrapLogDTO> xmlHandler = new XmlHandler<>(TrapLogDTO.class);
         String marshalledEvent = xmlHandler.marshal(trapLog);
         return marshalledEvent;
     }
}
