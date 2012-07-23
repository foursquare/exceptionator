// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.util

import javax.mail._
import javax.mail.internet._
import java.util.Properties


trait MailSender {
  def send(to: List[String], cc: List[String], subject: String, message: String): Unit
}

class ConcreteMailSender extends MailSender with Logger {
  val sender: MailSender = if (!Config.opt(_.getBoolean("email.test")).exists(_ == true)) {
    new JavaxMailSender
  } else {
    logger.info("Using a fake mailer, email.test is true")
    new LogMailSender
  }

  def send(to: List[String], cc: List[String], subject: String, message: String) {
    sender.send(to, cc, subject, message)
  }
}

class LogMailSender extends MailSender with Logger {
  def send(to: List[String], cc: List[String], subject: String, message: String) {
    logger.info("To:\n%s\nCC:\n%s\nSubject:\n%s\n\n%s".format(
      to.mkString("\n"),
      cc.mkString("\n"),
      subject,
      message
    ))
  }
}

class JavaxMailSender extends MailSender {
  val props = new Properties()
  props.setProperty("mail.transport.protocol", "smtp")
  props.setProperty("mail.host", Config.root.getString("email.host"))
  props.put("mail.smtp.auth", "true")
  props.put("mail.smtp.port", "465")
  props.put("mail.smtp.socketFactory.port", "465")
  props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
  props.put("mail.smtp.socketFactory.fallback", "false")
  props.setProperty("mail.smtp.quitwait", "false")
  val session = Session.getDefaultInstance(props, new Authenticator() {
    override def getPasswordAuthentication = new PasswordAuthentication(
      Config.opt(_.getString("email.user")).getOrElse(""),
      Config.opt(_.getString("email.password")).orElse(Config.opt(_.getString("email.pass"))).getOrElse("")
    )
  })

  def send(to: List[String], cc: List[String], subject: String, msg: String) {
    val message = new MimeMessage(session)
    message.setFrom(new InternetAddress(Config.opt(_.getString("email.from")).getOrElse("")))
    message.addRecipients(Message.RecipientType.TO, to.mkString(","))
    message.addRecipients(Message.RecipientType.CC, cc.mkString(","))
    message.setSubject(subject)
    message.setText(msg)
    Transport.send(message)
  }
}
