package seeds

type SeedRegister interface {
	Register(namespace string, seedId string) int
	Refresh(namespace string, seedId string, generatorId int) int
	Unregister(namespace string, seedId string, generatorId int)
}

type Seed struct {
	GeneratorId   int
	GeneratorBits uint
}

const maxGeneratorBit = 24
