package lila.tournament
package crud

final class CrudApi {

  def list = TournamentRepo uniques 30

  def one(id: String) = TournamentRepo uniqueById id

  def editForm(tour: Tournament) = CrudForm.apply fill CrudForm.Data(
    name = tour.name,
    homepageHours = ~tour.spotlight.flatMap(_.homepageHours),
    clockTime = tour.clock.limitInMinutes,
    clockIncrement = tour.clock.increment,
    minutes = tour.minutes,
    variant = tour.variant.id,
    mode = tour.mode.id,
    date = CrudForm.dateFormatter.print(tour.startsAt),
    dateHour = tour.startsAt.getHourOfDay,
    dateMinute = tour.startsAt.getMinuteOfHour,
    image = ~tour.spotlight.flatMap(_.iconImg),
    headline = tour.spotlight.??(_.headline),
    description = tour.spotlight.??(_.description))

  def update(old: Tournament, data: CrudForm.Data) =
    TournamentRepo update updateTour(old, data) void

  def createForm = CrudForm.apply

  def create(data: CrudForm.Data) =
    TournamentRepo insert updateTour(empty, data) void

  private val empty = Tournament.make(
    createdByUserId = "lichess",
    clock = TournamentClock(0, 0),
    minutes = 0,
    system = System.Arena,
    variant = chess.variant.Standard,
    position = chess.StartingPosition.initial,
    mode = chess.Mode.Rated,
    `private` = false,
    waitMinutes = 0)

  private def updateTour(tour: Tournament, data: CrudForm.Data) = {
    import data._
    val clock = TournamentClock((clockTime * 60).toInt, clockIncrement)
    val v = chess.variant.Variant.orDefault(variant)
    tour.copy(
      name = name,
      clock = clock,
      minutes = minutes,
      variant = v,
      mode = chess.Mode.orDefault(mode),
      startsAt = actualDate,
      schedule = Schedule(
        freq = Schedule.Freq.Unique,
        speed = Schedule.Speed.fromClock(clock),
        variant = v,
        position = chess.StartingPosition.initial,
        at = actualDate).some,
      spotlight = Spotlight(
        headline = headline,
        description = description,
        homepageHours = homepageHours.some.filterNot(0 ==),
        iconFont = none,
        iconImg = image.some).some)
  }
}