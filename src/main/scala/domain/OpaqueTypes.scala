package domain

object OpaqueTypes {
  
  opaque type Recurs = Int
  opaque type ExponentialDuration = zio.Duration

  extension(int: Int) {
    def toRecurs: Recurs = int
  }
  extension(recurs: Recurs) {
    def toInt: Int = recurs
  }

  extension(duration: zio.Duration) {
    def toExponentialDuration: ExponentialDuration = duration
  }
  extension(duration: ExponentialDuration) {
    def toZioDuration: zio.Duration = duration
  }
}
