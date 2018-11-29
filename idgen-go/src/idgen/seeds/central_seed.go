package seeds

import (
	"errors"
	"fmt"
	"time"
)

type CentralSeedServer struct {
	t *time.Timer
	seedChannel chan *CentralSeed
	seedMap map[string]*CentralSeed
}

type CentralSeed struct {
	seedRegister SeedRegister
	namespace string
	seedId string
	Seed
}

func NewCentralSeedServer() *CentralSeedServer{
	return &CentralSeedServer{}
}

func (server *CentralSeedServer) Start(d time.Duration) {
	if server.t == nil {
		server.seedChannel = make(chan *CentralSeed)
		server.seedMap = map[string]*CentralSeed{}
		server.t = time.AfterFunc(d, server.refresh)
		go server.add()
	}else{
		panic(errors.New("Central seeds started. "))
	}
}

func (server *CentralSeedServer) Stop(){
	server.t.Stop()
}

func (server *CentralSeedServer) add(){
	for{
		seed := <- server.seedChannel
		server.seedMap[seed.namespace] = seed
	}
}

func (server *CentralSeedServer) New(
	register SeedRegister, namespace string, seedId string, bits uint) *CentralSeed {
	generatorId := register.Register(namespace, seedId)
	if generatorId < 0{
		panic(errors.New(fmt.Sprintf("Can't create a new generator id for seed [id = %v].", generatorId)))
	}
	seed := &CentralSeed{
		register,
		namespace,
		seedId,
		Seed{generatorId, bits}}
	server.seedChannel <- seed
	return seed
}

func (server *CentralSeedServer)refresh(){
	for _, seed := range server.seedMap{
		seed.seedRegister.Refresh(seed.namespace, seed.seedId, seed.GeneratorId)
	}
}
