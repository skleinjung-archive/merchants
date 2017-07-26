import buymorecards as bmc

env = bmc.get_environment(bmc.Environment.TakeValidCards)
print(env.reset())

print(env.step(bmc.Color.Red, 5))
