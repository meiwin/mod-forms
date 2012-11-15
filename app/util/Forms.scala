package util

import play.api.data.{FormError, Mapping}
import play.api.data.validation.Constraint

/**
 * Created with IntelliJ IDEA.
 * User: meiwinfu
 * Date: 15/11/12
 * Time: 9:13 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * Provides a set of of operations related to `KeyedMapping` values.
 */
object KeyedMapping {

  def keys(key: String, data: Map[String, String]): Seq[String] = {
    val KeyPattern = ("^" + java.util.regex.Pattern.quote(key) + """\[(.+)\].*$""").r
    data.toSeq.collect { case (KeyPattern(dataKey), _) => dataKey }.sorted.distinct
  }
}

/**
 * A mapping for keyed elements.
 *
 * @param wrapped The wrapped mapping
 */
case class KeyedMapping[T](wrapped: Mapping[T], val key: String = "", val constraints: Seq[Constraint[Map[String, T]]] = Nil) extends Mapping[Map[String, T]] {

  /**
   * The Format expected for this field, if it exists.
   */
  override val format: Option[(String, Seq[Any])] = wrapped.format

  /**
   * Constructs a new Mapping based on this one, by adding new constraints.
   *
   * For example:
   * {{{
   *   import play.api.data._
   *   import validation.Constraints._
   *
   *   Form("phonenumber" -> text.verifying(required) )
   * }}}
   *
   * @param constraints the constraints to add
   * @return the new mapping
   */
  def verifying(addConstraints: Constraint[Map[String,T]]*): Mapping[Map[String,T]] = {
    this.copy(constraints = constraints ++ addConstraints.toSeq)
  }

  /**
   * Binds this field, i.e. construct a concrete value from submitted data.
   *
   * @param data the submitted data
   * @return either a concrete value of type `Map[String, T]` or a set of errors, if the binding failed
   */
  def bind(data: Map[String, String]): Either[Seq[FormError], Map[String,T]] = {
    val allErrorsOrItems: Seq[Either[Seq[FormError], (String,T)]] = KeyedMapping.keys(key, data).map(dataKey => wrapped.withPrefix(key + "[" + dataKey + "]").bind(data).fold(l => Left(l), r => Right((dataKey, r))))
    if (allErrorsOrItems.forall(_.isRight)) {
      Right(allErrorsOrItems.map(_.right.get)).right.flatMap(o => applyConstraints(o.toMap))
    } else {
      Left(allErrorsOrItems.collect { case Left(errors) => errors }.flatten)
    }
  }

  /**
   * Unbinds this field, i.e. transforms a concrete value to plain data.
   *
   * @param value the value to unbind
   * @return either the plain data or a set of errors, if the unbinding failed
   */
  def unbind(value: Map[String, T]): (Map[String, String], Seq[FormError]) = {
    val (datas, errors) = value.map { case (dataKey, t) => wrapped.withPrefix(key + "[" + dataKey + "]").unbind(t) }.unzip
    (datas.foldLeft(Map.empty[String, String])(_ ++ _), errors.flatten.toSeq ++ collectErrors(value))
  }

  /**
   * Constructs a new Mapping based on this one, adding a prefix to the key.
   *
   * @param prefix the prefix to add to the key
   * @return the same mapping, with only the key changed
   */
  def withPrefix(prefix: String): Mapping[Map[String,T]] = {
    addPrefix(prefix).map(newKey => this.copy(key = newKey)).getOrElse(this)
  }

  /**
   * Sub-mappings (these can be seen as sub-keys).
   */
  val mappings: Seq[Mapping[_]] = wrapped.mappings

}

object Forms {

  /**
   * Defines a keyed mapping.
   * {{{
   * Form(
   *    "name" -> keyed(text)
   * }}}
   *
   * @param mapping The mapping to make keyed.
   */
  def keyed[A](mapping: Mapping[A]): Mapping[Map[String, A]] = KeyedMapping(mapping)

}
