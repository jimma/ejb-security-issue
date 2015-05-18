/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.ws.jaxws.samples.wsse.policy.jaas;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.junit.Test;

/**
 * WS-Security Policy username ejb endpoint test case leveraging JAAS container integration and using digest passwords.
 * WS-SecurityPolicy 1.2 used for policies in the included wsdl contract.
 *
 * @author alessio.soldano@jboss.com
 * @author <a href="mailto:ema@redhat.com"/>Jim Ma<a>
 * @since 26-May-2011
 */
public final class UsernameAuthorizationDigestEjbTestCase
{

   //JBWS-3843
   @Test
   public void testConcurrent() throws Exception
   {
      ExecutorService executor = Executors.newFixedThreadPool(20);

      List<Callable<String>> taskList = new ArrayList<Callable<String>>();
      for (int i = 0; i < 20; i++)
      {
         taskList.add(new TestRunner());
      }
      List<Future<String>> resultList = executor.invokeAll(taskList);
      boolean passed = true;
      for (Future<String> future : resultList)
      {
         passed = passed && future.get().equals("Secure Hello World!");
      }
      assertTrue("Unexpected response from concurrent invocation", passed);

   }

   private class TestRunner implements Callable<String>
   {
      public String call() throws Exception
      {
         URL wsdlURL = new URL("http://localhost:8080/jaxws-samples-wsse-policy-username-jaas-ejb-digest/SecurityService/EJBDigestServiceImpl?wsdl");
         Service service = Service.create(wsdlURL, new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService"));
         ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
         setupWsse(proxy, "kermit");
         return proxy.sayHello();
      }

   }

   @Test
   public void testUnauthenticated() throws Exception
   {
      URL wsdlURL = new URL("http://localhost:8080/jaxws-samples-wsse-policy-username-jaas-ejb-digest/SecurityService/EJBDigestServiceImpl?wsdl");
      Service service = Service.create(wsdlURL, new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService"));
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy, "kermit");
      try
      {
         proxy.greetMe();
         fail("User kermit shouldn't be authenticated.");
      }
      catch (Exception e)
      {
         //OK
      }
   }

   private void setupWsse(ServiceIface proxy, String username)
   {
      ((BindingProvider)proxy).getRequestContext().put("ws-security.username", username);
      ((BindingProvider)proxy).getRequestContext().put("ws-security.callback-handler",
            "org.jboss.test.ws.jaxws.samples.wsse.policy.jaas.UsernameDigestPasswordCallback");
   }
}
