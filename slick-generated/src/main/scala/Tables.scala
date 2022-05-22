package 
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends Tables {
  val profile = com.example.slick.MyPostgwresProfile
}

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Examples.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Examples
   *  @param id Database column id SqlType(uuid), PrimaryKey
   *  @param name Database column name SqlType(text)
   *  @param name2 Database column name2 SqlType(text)
   *  @param isDeleted Database column is_deleted SqlType(bool), Default(false) */
  case class ExamplesRow(id: java.util.UUID, name: String, name2: String, isDeleted: Boolean = false)
  /** GetResult implicit for fetching ExamplesRow objects using plain SQL queries */
  implicit def GetResultExamplesRow(implicit e0: GR[java.util.UUID], e1: GR[String], e2: GR[Boolean]): GR[ExamplesRow] = GR{
    prs => import prs._
    ExamplesRow.tupled((<<[java.util.UUID], <<[String], <<[String], <<[Boolean]))
  }
  /** Table description of table examples. Objects of this class serve as prototypes for rows in queries. */
  class Examples(_tableTag: Tag) extends profile.api.Table[ExamplesRow](_tableTag, Some("service"), "examples") {
    def * = (id, name, name2, isDeleted).<>(ExamplesRow.tupled, ExamplesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(name), Rep.Some(name2), Rep.Some(isDeleted))).shaped.<>({r=>import r._; _1.map(_=> ExamplesRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(uuid), PrimaryKey */
    val id: Rep[java.util.UUID] = column[java.util.UUID]("id", O.PrimaryKey)
    /** Database column name SqlType(text) */
    val name: Rep[String] = column[String]("name")
    /** Database column name2 SqlType(text) */
    val name2: Rep[String] = column[String]("name2")
    /** Database column is_deleted SqlType(bool), Default(false) */
    val isDeleted: Rep[Boolean] = column[Boolean]("is_deleted", O.Default(false))
  }
  /** Collection-like TableQuery object for table Examples */
  lazy val Examples = new TableQuery(tag => new Examples(tag))
}
