package com.twitter.finagle.memcached.stress

import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.memcached.integration.InProcessMemcached
import com.twitter.finagle.memcached.protocol._
import com.twitter.finagle.memcached.protocol.text.Memcached
import com.twitter.finagle.memcached.util.ChannelBufferUtils._
import com.twitter.util.{Await, Time}
import java.net.InetSocketAddress
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class InterpreterServiceTest extends FunSuite with BeforeAndAfter {

  var server: InProcessMemcached = null
  var client: Service[Command, Response] = null

  before {
    server = new InProcessMemcached(new InetSocketAddress(0))
    val address = server.start().localAddress
    client = ClientBuilder()
      .hosts(address)
      .codec(new Memcached)
      .hostConnectionLimit(1)
      .build()
  }

  after {
    server.stop()
  }

  test("set & get") {
    val _key   = "key"
    val value = "value"
    val zero = "0"
    val start = System.currentTimeMillis
    (0 until 100) map { i =>
      val key = _key + i
      Await.result(client(Delete(key)))
      Await.result(client(Set(key, 0, Time.epoch, value)))
      Await.result(client(Get(Seq(key)))) === Values(Seq(Value(key, value, None, Some(zero))))
    }
    val end = System.currentTimeMillis
    // println("%d ms".format(end - start))
  }

}
