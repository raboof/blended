/*
 * Copyright 2014ff, WoQ - Way of Quality UG(mbH)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.woq.osgi.akka.persistence {

  package object protocol {

    implicit def primitive2Property(v: Any) : PersistenceProperty = v match {
      case b: Boolean  => BooleanProperty(b)
      case b: Byte     => ByteProperty(b)
      case s: Short    => ShortProperty(s)
      case i: Int      => IntProperty(i)
      case l: Long     => LongProperty(l)
      case f: Float    => FloatProperty(f)
      case d: Double   => DoubleProperty(d)
      case c: Char     => CharProperty(c)
      case s: String   => StringProperty(s)

      case x :: xs     => list2Property(x :: xs)
    }

    private[protocol] def list2Property[T](l : List[T]) = ListProperty(l.map(primitive2Property(_)))
  }

  package protocol {

    sealed class PersistenceProperty
    sealed case class BooleanProperty(b: Boolean) extends PersistenceProperty
    sealed case class ByteProperty(b: Byte) extends PersistenceProperty
    sealed case class ShortProperty(s: Short) extends PersistenceProperty
    sealed case class IntProperty(i: Int) extends PersistenceProperty
    sealed case class LongProperty(l: Long) extends PersistenceProperty
    sealed case class FloatProperty(f: Float) extends PersistenceProperty
    sealed case class DoubleProperty(d: Double) extends PersistenceProperty
    sealed case class CharProperty(c: Char) extends PersistenceProperty
    sealed case class StringProperty(s: String) extends PersistenceProperty
    sealed case class ListProperty[T <: PersistenceProperty](values: List[T]) extends PersistenceProperty

    abstract class DataObject(uuid : String) {
    }
  }
}
