package seeds

import (
	"fmt"
	"github.com/magiconair/properties/assert"
	"strconv"
	"testing"
	"time"
)

type MemorySeedRegister struct {
	seedMap map[string]*[]string
}

func (r *MemorySeedRegister) Register(namespace string, seedId string) int {
	list := r.seedMap[namespace]
	if list != nil{
		for i:=0; i<len(*list); i++{
			if (*list)[i] == seedId{
				return i
			}
		}
	}else{
		l := make([]string, 0)
		list = &l
	}
	newList := append(*list, seedId)
	r.seedMap[namespace] = &newList
	return len(newList) - 1
}

func (r *MemorySeedRegister) Refresh(namespace string, seedId string, generatorId int) int{
	list := r.seedMap[namespace]
	if list != nil && (*list)[generatorId] == seedId{
		return generatorId
	}
	return r.Register(namespace, seedId)
}

func (r *MemorySeedRegister) Unregister(namespace string, seedId string, generatorId int){
	list := r.seedMap[namespace]
	if list != nil{
		for i:=0; i<len(*list); i++{
			if (*list)[i] == seedId{
				(*list)[i] = ""
				return
			}
		}
	}
}

func TestSimple(t *testing.T) {
	count := 10000
	server := NewCentralSeedServer()
	register := &MemorySeedRegister{make(map[string]*[]string)}
	server.Start( time.Second )
	for i:=0; i<count; i++{
		seed := server.New(register, "TestSimple", "TEST_"+strconv.Itoa(i), 10)
		assert.Equal(t, seed.GeneratorId, i)
		assert.Equal(t, seed.GeneratorBits, uint(10))
	}
}

type MemoryRedisAdapter struct {
	seedMap map[string]string
}

func (m *MemoryRedisAdapter) Get(key string) string  {
	return m.seedMap[key]
}

func (m *MemoryRedisAdapter) Set(key string, value string, seconds uint) bool  {
	m.seedMap[key] = value
	return true
}

func (m *MemoryRedisAdapter) Expire(key string, seconds uint) bool  {
	return true
}

func (m *MemoryRedisAdapter) Del(key string) bool  {
	delete(m.seedMap, key)
	return true
}

func TestRedisSeedRegister(t *testing.T) {
	count := 1024
	adapter := new(MemoryRedisAdapter)
	adapter.seedMap = make(map[string]string)
	register := NewRedisSeedRegister(adapter, 0, uint(count-1))
	server := NewCentralSeedServer()
	server.Start( time.Second )
	for i:=0; i<count; i++{
		seed := server.New(register, "TestRedisSeedRegister", "TEST_"+strconv.Itoa(i), 10)
		assert.Equal(t, seed.GeneratorId, i)
		assert.Equal(t, seed.GeneratorBits, uint(10))
	}

	defer func() {
		if r := recover(); r == nil {
			t.Errorf("The code did not panic")
		}
	}()
	server.New(register, "TestRedisSeedRegister", "TEST_"+strconv.Itoa(count), 10)
}

func TestNewGoRedisAdapter(t *testing.T){
	count := 20
	adapter := NewGoRedisAdapter([]string{"172.18.90.227:7000","172.18.90.227:7001","172.18.90.228:7000","172.18.90.228:7001"}, "YZclskker2sc")
	register := NewRedisSeedRegister(adapter, 0, uint(count-1))
	server := NewCentralSeedServer()
	server.Start( time.Second )

	for i:=0; i<=count; i++{
		register.Unregister("TestNewGoRedisAdapter", "TEST_"+strconv.Itoa(i), i)
	}

	for i:=0; i<count; i++{
		fmt.Println("test register:", i)
		seed := server.New(register, "TestNewGoRedisAdapter", "TEST_"+strconv.Itoa(i), 10)
		assert.Equal(t, seed.GeneratorId, i)
		assert.Equal(t, seed.GeneratorBits, uint(10))
	}

	defer func() {
		if r := recover(); r == nil {
			t.Errorf("The code did not panic")
		}
	}()
	seed := server.New(register, "TestNewGoRedisAdapter", "TEST_"+strconv.Itoa(count), 10)
	fmt.Println(seed.GeneratorId)
	for i:=0; i<=count; i++{
		register.Unregister("TestNewGoRedisAdapter", "TEST_"+strconv.Itoa(i), i)
	}
}